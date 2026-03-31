package com.cinebuzz.service;

import com.cinebuzz.dto.request.CityRequestDto;
import com.cinebuzz.dto.response.CityResponseDto;
import com.cinebuzz.entity.City;
import com.cinebuzz.exception.AlreadyExistsException;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    public CityResponseDto createCity(CityRequestDto dto) {
        if (cityRepository.existsByName(dto.getName())) {
            throw new AlreadyExistsException("City already exists: " + dto.getName());
        }
        City city = new City();
        city.setName(dto.getName());
        city.setState(dto.getState());
        City saved = cityRepository.save(city);
        return mapToDto(saved);
    }

    public List<CityResponseDto> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CityResponseDto updateCity(Long id, CityRequestDto dto) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        city.setName(dto.getName());
        city.setState(dto.getState());
        City updated = cityRepository.save(city);
        return mapToDto(updated);
    }

    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException("City not found with id: " + id);
        }
        cityRepository.deleteById(id);
    }

    // private helper — reuse instead of repeating mapping code
    private CityResponseDto mapToDto(City city) {
        return new CityResponseDto(
                city.getId(),
                city.getName(),
                city.getState()
        );
    }
}