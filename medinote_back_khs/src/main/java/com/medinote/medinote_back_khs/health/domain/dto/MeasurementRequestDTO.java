package com.medinote.medinote_back_khs.health.domain.dto;

import com.medinote.medinote_back_khs.health.domain.en.MeasurementStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementRequestDTO {

//    @NotNull(message = "회원ID(은)는 필수 입니다.")
//    private Long memberId;

    @NotNull(message = "성별(은)는 필수 입니다.")
    private MeasurementStatus gender;

    @NotNull(message = "흡연여부(은)는 필수 입니다.")
    private boolean smoking;

    @NotNull(message = "음주여부(은)는 필수 입니다.")
    private boolean drinking;

    @Min(value = 0, message = "주당 최소 음주 횟수 0회입니다")
    @Max(value = 30, message = "주당 최대 음주 횟수 30회입니다")
    private Integer drinkingPerWeek;

    @Min(value = 1, message = "1회당 최소 음주 횟수 1회입니다")
    @Max(value = 20, message = "1회당 최대 음주 횟수 20회입니다")
    private Integer drinkingPerOnce;

    @NotNull(message = "만성질환 여부는 필수입니다")
    private Boolean chronicDiseaseYn;

    @NotNull(message = "알레르기 여부는 필수입니다")
    private Boolean allergyYn;

    @NotNull(message = "복용약물 여부는 필수입니다")
    private Boolean medicationYn;

    @Positive(message = "신장은 양수여야 합니다")
    @Max(value = 300, message = "입력 가능한 최대 신장은 300cm 입니다")
    private Double height;

    @Positive(message = "체중은 양수여야 합니다")
    @Max(value = 500, message = "입력 가능한 최대 체중은 500kg 입니다")
    private Double weight;

    @Min(value = 50, message = "수축기 혈압은 50mmHg 이상이어야 합니다")
    @Max(value = 300, message = "수축기 혈압은 300mmHg 이하여야 합니다")
    private Integer bloodPressureSystolic;

    @Min(value = 30, message = "이완기 혈압은 30mmHg 이상이어야 합니다")
    @Max(value = 200, message = "이완기 혈압은 200mmHg 이하여야 합니다")
    private Integer bloodPressureDiastolic;

    @Positive(message = "혈당은 양수여야 합니다")
    @Max(value = 1000, message = "혈당은 1000mg/dL 이하여야 합니다")
    private Double bloodSugar;

    @Min(value = 30, message = "심박수는 30bpm 이상이어야 합니다.")
    @Max(value = 250, message = "심박수는 250bpm 이하여야 합니다.")
    private Integer heartRate;          // 심박수 (bpm)

    @Positive(message = "수면 시간은 0 이상이어야 합니다.")
    @Max(value = 24, message = "하루 수면 시간은 24시간을 초과할 수 없습니다.")
    private Double sleepHours;          // 수면 시간 (시간 단위)


}
