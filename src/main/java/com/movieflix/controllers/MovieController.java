package com.movieflix.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieflix.dto.MovieDto;
import com.movieflix.entities.Movie;
import com.movieflix.exceptions.EmptyFileException;
import com.movieflix.service.MovieService;
import com.movieflix.util.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
/*
Author: Kamal Thapa
 */
@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;


    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(@RequestPart MultipartFile file, @RequestPart String movieDto) throws IOException, EmptyFileException {



        if(file.isEmpty()){
            throw new EmptyFileException("File is empty! please send another file");
        }
        MovieDto dto = convertToMovieDto(movieDto);

        return new ResponseEntity<>(movieService.addMovie(dto, file), HttpStatus.CREATED);



    }

    @GetMapping("/{movieId}")
//    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId){
    public Response getMovieHandler(@PathVariable Integer movieId){

        Response response =  new Response();

        //return  ResponseEntity.ok(movieService.getMovie(movieId));
        MovieDto movieDto = movieService.getMovie(movieId);

        if(movieDto != null){
            response.setMessage("Data Found SuccessFully");
            response.setStatus("True");
            response.setObject(movieDto);
        }else{
            response.setMessage("Data Not Found");
            response.setStatus("False");
        }

        return response;
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMovieHandler(){

        return  ResponseEntity.ok(movieService.getAllMovies());
    }

    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId, @RequestPart MultipartFile file, @RequestPart String movieDtoObj) throws IOException {

        if(file.isEmpty()){
            file = null;
        }
        MovieDto movieDto = convertToMovieDto(movieDtoObj);
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieDto, file));
    }

    @DeleteMapping("/delete/{movieId}")
    public  ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {

        return  ResponseEntity.ok(movieService.deleteMovie(movieId));
    }


    // converting Json String to Object Type
    private MovieDto convertToMovieDto(String movieDtoObject) throws JsonProcessingException {

        ObjectMapper objectMapper =  new ObjectMapper();

       return  objectMapper.readValue(movieDtoObject, MovieDto.class);

    }
}
