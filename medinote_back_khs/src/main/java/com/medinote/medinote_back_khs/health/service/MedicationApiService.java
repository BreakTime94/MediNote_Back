package com.medinote.medinote_back_khs.health.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationApiDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.mapper.MedicationMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MedicationApiService {

  private final MedicationRepository medicationRepository;
  private final MedicationMapper medicationMapper;
  //API 서버와 통신할 때 사용
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${openapi.mfds.key}") //secret/api.yml API키
  private String apiKey;

  public void fetchAndSaveMedication() throws JsonProcessingException{
    int pageNo = 1;
    int numOfRows = 100; //api에서 한 번에 가져올 수 있는 최대 건수
    boolean hasMore = true;

    //JSON 파싱(json -> java)
    ObjectMapper mapper = new ObjectMapper();

    while (hasMore) {
      String url = "http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList"
              + "?serviceKey=" + apiKey
              + "&pageNo=" + pageNo
              + "&numOfRows=" + numOfRows
              + "&type=json";

      //호출
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      //api의 응답(response)을 문자열로 받음

      if(response.getStatusCode().is2xxSuccessful()) {
        // 응답 본문(response.getBody())에서 body -> items 배열까지 찾아감
        JsonNode body = mapper.readTree(response.getBody()).path("body");
        JsonNode items = body.path("items");  //path: 중첩된 JSON에서 특정 경로로 안전하게 접근

        if(items.isArray() && items.size() > 0) {
          //JSON → DTO → Entity 변환 후 저장
          for (JsonNode item : items) {
            String drugCode = item.path("itemSeq").asText();

            //중복체쿠(식약 코드가 존재하지 않을 때)
            if (!medicationRepository.existsByDrugCode((drugCode))) {
              // JSON → DTO 변환
              MedicationApiDTO dto = mapper.treeToValue(item, MedicationApiDTO.class);

              // DTO → Entity 변환 (MapStruct)
              Medication med = medicationMapper.toEntity(dto);

              medicationRepository.save(med);
            }
          }
          pageNo++;

          try {
            Thread.sleep(200);  //호출 제한
          }catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("error : " , e);
          }

        } else {
          hasMore = false;  //마지막 페이지
        }
      } else {
        hasMore = false;  //응답 실패지 멈춤
      }
    }
  }


  //==================복용약 리스트 관련===============================================

  //리스트 조회
  @Transactional(readOnly = true)
  public Page<MedicationResponseDTO> getMedicationList(Pageable pageable) throws JsonProcessingException{
    return medicationRepository.findAll(pageable).map(medicationMapper::toResponseDTO);
  }

  @Transactional(readOnly = true)
  public List<MedicationResponseDTO> searchMedication(String keyword) {
    return medicationMapper.toResponseDTOList(medicationRepository.findByNameKoContaining(keyword));
  }

  @Transactional(readOnly = true)
  public Optional<MedicationResponseDTO> getMedicationById(Long id) {
    return medicationRepository.findById(id).map(medicationMapper::toResponseDTO);
    // Medication → MedicationResponseDTO 변환
    // 불필요한 필드들(createdAt, internalNotes 등) 제거
  }
}
