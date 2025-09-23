package com.medinote.medinote_back_khs.health.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationApiDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.mapper.MedicationMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MedicationApiService {

  private final MedicationRepository medicationRepository;
  private final MedicationMapper medicationMapper;

  //API 서버와 통신할 때 사용
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${openapi.mfds.key}") //secret/api.yml API키
  private String apiKey;

  public void fetchAndSaveMedication() throws JsonProcessingException {
    String url = "http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList"
            + "?serviceKey=" + apiKey
            + "&pageNo=1&numOfRows=10&type=json";

    //호출
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    //api의 응답(response)을 문자열로 받음

    if(response.getStatusCode().is2xxSuccessful()) {
      //JSON 파싱
      ObjectMapper mapper = new ObjectMapper();
      // 응답 본문(response.getBody())에서 body -> items 배열까지 찾아감
      JsonNode items = mapper.readTree(response.getBody())
              .path("body")
              .path("items");

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
    }
  }

}
