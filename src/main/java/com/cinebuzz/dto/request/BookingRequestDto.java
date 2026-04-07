package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingRequestDto {

    @NotEmpty(message = "Select at least one seat")
    @Size(min = 1, max = 10, message = "You can book between 1 and 10 seats")
    private List<@NotNull(message = "Seat id cannot be null") @Positive(message = "Each seat id must be positive") Long> showtimeSeatIds;
}
