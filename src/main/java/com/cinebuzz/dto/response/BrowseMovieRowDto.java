package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One movie playing in a city, with how many showtimes exist there (for the home grid).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrowseMovieRowDto {

    private MovieResponseDto movie;
    private long showtimeCount;
}
