package com.cinebuzz.service;

import com.cinebuzz.dto.request.ScreenRequestDto;
import com.cinebuzz.dto.response.ScreenResponseDto;
import com.cinebuzz.entity.Screen;
import com.cinebuzz.entity.Seat;
import com.cinebuzz.entity.Theatre;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.repository.ScreenRepository;
import com.cinebuzz.repository.SeatRepository;
import com.cinebuzz.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Transactional
    public ScreenResponseDto createScreen(ScreenRequestDto dto) {
        Theatre theatre = theatreRepository.findById(dto.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + dto.getTheatreId()));

        Screen screen = new Screen();
        screen.setName(dto.getName());
        screen.setTotalRows(dto.getTotalRows());
        screen.setSeatsPerRow(dto.getSeatsPerRow());
        screen.setTheatre(theatre);
        Screen saved = screenRepository.save(screen);

        // auto-generate seats
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < dto.getTotalRows(); row++) {
            char rowLabel = (char) ('A' + row);
            for (int num = 1; num <= dto.getSeatsPerRow(); num++) {
                Seat seat = new Seat();
                seat.setRowLabel(String.valueOf(rowLabel));
                seat.setSeatNumber(num);
                seat.setSeatLabel(rowLabel + String.valueOf(num));
                seat.setScreen(saved);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);

        return mapToDto(saved);
    }

    public List<ScreenResponseDto> getScreensByTheatre(Long theatreId) {
        if (!theatreRepository.existsById(theatreId)) {
            throw new ResourceNotFoundException("Theatre not found with id: " + theatreId);
        }
        return screenRepository.findByTheatreId(theatreId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScreen(Long id) {
        if (!screenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screen not found with id: " + id);
        }
        screenRepository.deleteById(id);
    }

    private ScreenResponseDto mapToDto(Screen screen) {
        return new ScreenResponseDto(
                screen.getId(),
                screen.getName(),
                screen.getTotalRows(),
                screen.getSeatsPerRow(),
                screen.getTotalRows() * screen.getSeatsPerRow(),
                screen.getTheatre().getId(),
                screen.getTheatre().getName()
        );
    }
}