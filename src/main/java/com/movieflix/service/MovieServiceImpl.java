package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.entities.Movie;
import com.movieflix.exceptions.FileExistException;
import com.movieflix.exceptions.MovieNotFoundException;
import com.movieflix.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImpl implements  MovieService{

    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }


    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. to upload the file

       if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
           throw  new FileExistException("File Already Exist please enter another file name!");
       }

       String uploadedFileName =  fileService.uploadFile(path, file);


        // 2. set the value of filed 'poster' as filename
        movieDto.setPoster(uploadedFileName);

        // 3. map dto to movie object
        Movie movie = new Movie(
                null, movieDto.getTitle(),
                movieDto.getDirector(),movieDto.getStudio(),
                movieDto.getMovieCast(),movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 4. save the movie object -> it will return saved movie object

        Movie savedMovie = movieRepository.save(movie);

        // 5. generate the posterUrl
        String posterUrl = baseUrl + "/file/"+ uploadedFileName;


        // 6. map movie object to dto object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(), savedMovie.getTitle(),
                savedMovie.getDirector(),savedMovie.getStudio(),
                savedMovie.getMovieCast(),savedMovie.getReleaseYear(),
                savedMovie.getPoster(), posterUrl
                );




        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {

        // 1. check the data in DB and if exist fetch the data of given ID
       Movie movie =  movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("movie not found with id = "+movieId));

        // 2. generate posterUrl
        String  posterUrl = baseUrl + "/file/"+ movie.getPoster();

        // 3. map the MovieDto object and return it
        MovieDto response = new MovieDto(
                movie.getMovieId(), movie.getTitle(),
                movie.getDirector(),movie.getStudio(),
                movie.getMovieCast(),movie.getReleaseYear(),
                movie.getPoster(), posterUrl
        );
        return  response;

//        Optional<Movie>movie =  movieRepository.findById(movieId);
//
//        if (movie.isPresent()) {
//            String poster = movie.get().getPoster();
//            String  posterUrl = baseUrl + "/file/"+ poster;
//            MovieDto response = new MovieDto(
//                movie.get().getMovieId(), movie.get().getTitle(),
//                movie.get().getDirector(),movie.get().getStudio(),
//                movie.get().getMovieCast(),movie.get().getReleaseYear(),
//                movie.get().getPoster(), posterUrl);
//            return response;
//        } else {
//            System.out.println("Movie not found");
//            return null;
//        }


    }

    @Override
    public List<MovieDto> getAllMovies() {
        // 1. fetch all data from DB

     List<Movie> movies =  movieRepository.findAll();

     // create a new ArrayList of MovieDto for mapping
        List<MovieDto> movieDtos =  new ArrayList<>();

        // 2. iterate through th list and generate posterUrl for each movie object and map to movieDto object

        for(Movie movie : movies){
            String posterUrl =  baseUrl + "/file/"+ movie.getPoster();

            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(), movie.getTitle(),
                    movie.getDirector(),movie.getStudio(),
                    movie.getMovieCast(),movie.getReleaseYear(),
                    movie.getPoster(), posterUrl
            );
            movieDtos.add(movieDto);
        }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. check if movie object exists with given movieId
        Movie mv =  movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("movie not found with id = "+movieId));


        // 2. if file is null , do nothing
        // if file is not null, then delete existing file associated with the record
        // and upload the new file

        String fileName = mv.getPoster();
        if (file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path , file);
        }


        // 3. set moviedto's postrer value, according to step2
        movieDto.setPoster(fileName);

        // 4. map it to movie object
        Movie movie = new Movie(
                mv.getMovieId(), movieDto.getTitle(),
                movieDto.getDirector(),movieDto.getStudio(),
                movieDto.getMovieCast(),movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 5. save the movie object -> return saved mpvie object

        Movie updateMovie = movieRepository.save(movie);

        // 6. generate posterUrl for it
        String posterUrl = baseUrl + "/file/"+ fileName;

        // 7. map to movieDto and return it
        MovieDto response = new MovieDto(
                movie.getMovieId(), movie.getTitle(),
                movie.getDirector(),movie.getStudio(),
                movie.getMovieCast(),movie.getReleaseYear(),
                movie.getPoster(), posterUrl
        );



        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {

        // 1. check if movie object exist in DB
        Movie mv =  movieRepository.findById(movieId).orElseThrow(() -> new  MovieNotFoundException("movie not found with id = "+movieId));
        Integer id = mv.getMovieId();

        // 2. delete the file associated with that obejct
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));

        // 3. delete the movie object
        movieRepository.delete(mv);
        return "Movie is deleted with id = "+id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        Page<Movie> moviePages = movieRepository.findAll(pageable);

        List<Movie> movie = moviePages.getContent();
// incomplete more work in queue

        return null;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String direction) {
        return null;
    }
}
