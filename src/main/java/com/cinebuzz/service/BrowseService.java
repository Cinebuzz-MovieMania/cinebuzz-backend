package com.cinebuzz.service;

import com.cinebuzz.dto.response.BrowseMovieRowDto;
import com.cinebuzz.dto.response.MovieResponseDto;
import com.cinebuzz.dto.response.ShowtimeResponseDto;
import com.cinebuzz.entity.Movie;
import com.cinebuzz.entity.Showtime;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.exception.ValidationException;
import com.cinebuzz.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BrowseService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ShowtimeService showtimeService;

    @Transactional(readOnly = true)
    public List<BrowseMovieRowDto> listMoviesForCity(Long cityId) {
        if (cityId == null) {
            throw new ValidationException("cityId is required.");
        }
        List<Movie> movies = showtimeRepository.findDistinctMoviesPlayingInCity(cityId);
        List<Object[]> countRows = showtimeRepository.countShowtimesPerMovieInCity(cityId);
        Map<Long, Long> countByMovieId = new HashMap<>();
        for (Object[] row : countRows) {
            if (row[0] != null && row[1] != null) {
                countByMovieId.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }
        return movies.stream()
                .map(m -> new BrowseMovieRowDto(
                        toMovieDto(m),
                        countByMovieId.getOrDefault(m.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponseDto> listShowtimesForCityMovieAndDate(Long cityId, Long movieId, LocalDate date) {
        if (cityId == null || movieId == null || date == null) {
            throw new ValidationException("cityId, movieId, and date are required.");
        }
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Showtime> rows = showtimeRepository.findByCityMovieAndDateRange(cityId, movieId, dayStart, dayEnd);
        return rows.stream().map(showtimeService::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShowtimeResponseDto getShowtimeById(Long id) {
        Showtime st = showtimeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return showtimeService.mapToDto(st);
    }

    private static MovieResponseDto toMovieDto(Movie m) {
        return new MovieResponseDto(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getGenre(),
                m.getLanguage(),
                m.getDurationMinutes(),
                m.getReleaseDate(),
                m.getPosterUrl()
        );
    }
}
