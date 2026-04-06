package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeatHoldRequestDto {

    @NotEmpty(message = "Select at least one seat.")
    private List<Long> showtimeSeatIds;
}
