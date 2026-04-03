package com.cinebuzz.service;

import com.cinebuzz.dto.request.ShowtimeRequestDto;
import com.cinebuzz.dto.response.ShowtimeResponseDto;
import com.cinebuzz.entity.Movie;
import com.cinebuzz.entity.Screen;
import com.cinebuzz.entity.Seat;
import com.cinebuzz.entity.Showtime;
import com.cinebuzz.entity.ShowtimeSeat;
import com.cinebuzz.enums.SeatStatus;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.exception.ValidationException;
import com.cinebuzz.repository.MovieRepository;
import com.cinebuzz.repository.ScreenRepository;
import com.cinebuzz.repository.SeatRepository;
import com.cinebuzz.repository.ShowtimeRepository;
import com.cinebuzz.repository.ShowtimeSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private SeatRepository seatRepository;

    /** Minimum gap (minutes) between end of one show and start of the next on the same screen. */
    public static final int MIN_GAP_BETWEEN_SHOWS_MINUTES = 5;

    @Transactional
    public ShowtimeResponseDto createShowtime(ShowtimeRequestDto dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + dto.getMovieId()));
        Screen screen = screenRepository.findById(dto.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + dto.getScreenId()));

        LocalDateTime start = dto.getStartTime();
        LocalDateTime end = start.plusMinutes(movie.getDurationMinutes());

        validateShowtimeWindow(screen, start, end);

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setScreen(screen);
        showtime.setStartTime(start);
        showtime.setEndTime(end);
        showtime.setPrice(dto.getPrice());
        Showtime saved = showtimeRepository.save(showtime);

        // auto-generate ShowtimeSeats for every seat in the screen
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        List<ShowtimeSeat> showtimeSeats = new ArrayList<>();
        for (Seat seat : seats) {
            ShowtimeSeat showtimeSeat = new ShowtimeSeat();
            showtimeSeat.setShowtime(saved);
            showtimeSeat.setSeat(seat);
            showtimeSeat.setStatus(SeatStatus.AVAILABLE);
            showtimeSeats.add(showtimeSeat);
        }
        showtimeSeatRepository.saveAll(showtimeSeats);

        return mapToDto(saved);
    }

    /**
     * End time is always start + movie duration. Ensures no conflict with existing shows on the same screen,
     * including a mandatory gap ({@link #MIN_GAP_BETWEEN_SHOWS_MINUTES} min) after each show ends.
     */
    private void validateShowtimeWindow(Screen screen, LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ValidationException("Computed end time must be after start time.");
        }

        LocalDateTime newEndPlusGap = end.plusMinutes(MIN_GAP_BETWEEN_SHOWS_MINUTES);
        if (showtimeRepository.countConflictingOnScreenWithGap(
                screen.getId(), start, newEndPlusGap, MIN_GAP_BETWEEN_SHOWS_MINUTES) > 0) {
            throw new ValidationException(
                    "This screen already has a show in that window, or the "
                            + MIN_GAP_BETWEEN_SHOWS_MINUTES
                            + "-minute gap after another show is not satisfied. Choose a different start time.");
        }
    }

    public List<ShowtimeResponseDto> getAllShowtimes() {
        return showtimeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ShowtimeResponseDto getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return mapToDto(showtime);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Showtime not found with id: " + id);
        }
        showtimeRepository.deleteById(id);
    }

    /** Maps entity with seat counts (used by admin + browse APIs). */
    public ShowtimeResponseDto mapToDto(Showtime showtime) {
        long availableSeats = showtimeSeatRepository
                .countByShowtimeIdAndStatus(showtime.getId(), SeatStatus.AVAILABLE);
        int totalSeats = showtime.getScreen().getTotalRows() * showtime.getScreen().getSeatsPerRow();
        return new ShowtimeResponseDto(
                showtime.getId(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getScreen().getId(),
                showtime.getScreen().getName(),
                showtime.getScreen().getTheatre().getName(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getPrice(),
                totalSeats,
                (int) availableSeats
        );
    }
}