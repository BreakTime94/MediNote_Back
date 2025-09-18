package com.medinote.medinote_back_khs.health.domain.dto;

import com.medinote.medinote_back_khs.health.domain.en.MeasurementStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementRequestDTO {

    @NotNull(message = "회원ID(은)는 필수 입니다.")
    private Long memberId;

    @NotNull(message = "성별(은)는 필수 입니다.")
    private MeasurementStatus gender;

}
