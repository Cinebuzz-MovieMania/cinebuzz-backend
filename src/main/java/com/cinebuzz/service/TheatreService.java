package com.cinebuzz.service;

import com.cinebuzz.dto.request.TheatreRequestDto;
import com.cinebuzz.dto.response.TheatreResponseDto;
import com.cinebuzz.entity.City;
import com.cinebuzz.entity.Theatre;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.repository.CityRepository;
import com.cinebuzz.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheatreService {

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private CityRepository cityRepository;

    public TheatreResponseDto createTheatre(TheatreRequestDto dto) {
        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + dto.getCityId()));
        Theatre theatre = new Theatre();
        theatre.setName(dto.getName());
        theatre.setAddress(dto.getAddress());
        theatre.setCity(city);
        Theatre saved = theatreRepository.save(theatre);
        return mapToDto(saved);
    }

    public List<TheatreResponseDto> getAllTheatres() {
        return theatreRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TheatreResponseDto getTheatreById(Long id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + id));
        return mapToDto(theatre);
    }

    public TheatreResponseDto updateTheatre(Long id, TheatreRequestDto dto) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + id));
        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + dto.getCityId()));
        theatre.setName(dto.getName());
        theatre.setAddress(dto.getAddress());
        theatre.setCity(city);
        Theatre updated = theatreRepository.save(theatre);
        return mapToDto(updated);
    }

    public void deleteTheatre(Long id) {
        if (!theatreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Theatre not found with id: " + id);
        }
        theatreRepository.deleteById(id);
    }

    private TheatreResponseDto mapToDto(Theatre theatre) {
        return new TheatreResponseDto(
                theatre.getId(),
                theatre.getName(),
                theatre.getAddress(),
                theatre.getCity().getId(),
                theatre.getCity().getName()
        );
    }
}