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
import com.cinebuzz.repository.MovieRepository;
import com.cinebuzz.repository.ScreenRepository;
import com.cinebuzz.repository.SeatRepository;
import com.cinebuzz.repository.ShowtimeRepository;
import com.cinebuzz.repository.ShowtimeSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public ShowtimeResponseDto createShowtime(ShowtimeRequestDto dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + dto.getMovieId()));
        Screen screen = screenRepository.findById(dto.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + dto.getScreenId()));

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setScreen(screen);
        showtime.setStartTime(dto.getStartTime());
        showtime.setEndTime(dto.getEndTime());
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
            showtimeSeat.setLockedUntil(null);
            showtimeSeats.add(showtimeSeat);
        }
        showtimeSeatRepository.saveAll(showtimeSeats);

        return mapToDto(saved);
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

    private ShowtimeResponseDto mapToDto(Showtime showtime) {
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