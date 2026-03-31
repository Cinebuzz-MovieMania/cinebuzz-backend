package com.cinebuzz.service;

import com.cinebuzz.dto.request.MoviePersonRequestDto;
import com.cinebuzz.dto.request.PersonRequestDto;
import com.cinebuzz.dto.response.MoviePersonResponseDto;
import com.cinebuzz.dto.response.PersonResponseDto;
import com.cinebuzz.entity.Movie;
import com.cinebuzz.entity.MoviePerson;
import com.cinebuzz.entity.Person;
import com.cinebuzz.exception.AlreadyExistsException;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.repository.MoviePersonRepository;
import com.cinebuzz.repository.MovieRepository;
import com.cinebuzz.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MoviePersonRepository moviePersonRepository;

    @Autowired
    private MovieRepository movieRepository;

    public PersonResponseDto createPerson(PersonRequestDto dto) {
        Person person = new Person();
        person.setName(dto.getName());
        person.setBio(dto.getBio());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setNationality(dto.getNationality());
        person.setProfilePictureUrl(dto.getProfilePictureUrl());
        person.setProfilePictureKey(dto.getProfilePictureKey());
        Person saved = personRepository.save(person);
        return mapToDto(saved);
    }

    public List<PersonResponseDto> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PersonResponseDto getPersonById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
        return mapToDto(person);
    }

    public PersonResponseDto updatePerson(Long id, PersonRequestDto dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
        person.setName(dto.getName());
        person.setBio(dto.getBio());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setNationality(dto.getNationality());
        person.setProfilePictureUrl(dto.getProfilePictureUrl());
        person.setProfilePictureKey(dto.getProfilePictureKey());
        Person updated = personRepository.save(person);
        return mapToDto(updated);
    }

    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }

    public MoviePersonResponseDto addPersonToMovie(Long movieId, MoviePersonRequestDto dto) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + dto.getPersonId()));
        if (moviePersonRepository.existsByMovieIdAndPersonId(movieId, dto.getPersonId())) {
            throw new AlreadyExistsException("Person already added to this movie");
        }
        MoviePerson moviePerson = new MoviePerson();
        moviePerson.setMovie(movie);
        moviePerson.setPerson(person);
        moviePerson.setRole(dto.getRole());
        moviePerson.setCharacterName(dto.getCharacterName());
        moviePerson.setBillingOrder(dto.getBillingOrder());
        MoviePerson saved = moviePersonRepository.save(moviePerson);
        return mapToMoviePersonDto(saved);
    }

    public void removePersonFromMovie(Long movieId, Long personId) {
        MoviePerson moviePerson = moviePersonRepository
                .findByMovieIdAndPersonId(movieId, personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found in this movie"));
        moviePersonRepository.delete(moviePerson);
    }

    public List<MoviePersonResponseDto> getCastByMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return moviePersonRepository.findByMovieIdOrderByBillingOrder(movieId)
                .stream()
                .map(this::mapToMoviePersonDto)
                .collect(Collectors.toList());
    }

    private PersonResponseDto mapToDto(Person person) {
        return new PersonResponseDto(
                person.getId(),
                person.getName(),
                person.getBio(),
                person.getDateOfBirth(),
                person.getNationality(),
                person.getProfilePictureUrl()
        );
    }

    private MoviePersonResponseDto mapToMoviePersonDto(MoviePerson mp) {
        return new MoviePersonResponseDto(
                mp.getId(),
                mp.getPerson().getId(),
                mp.getPerson().getName(),
                mp.getPerson().getProfilePictureUrl(),
                mp.getRole(),
                mp.getCharacterName(),
                mp.getBillingOrder()
        );
    }
}