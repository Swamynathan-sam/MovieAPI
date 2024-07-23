package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.entity.Movie;
import com.movieflix.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImpl implements MovieService{

    @Value("${project.poster}")
    private String path;

    @Value(("${base.url}"))
    private String baseUrl;

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    private final FileService fileService;

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        //1. upload the file
        //If file already exists will throw exception
        if(Files.exists(Paths.get(path + File.separator+file.getOriginalFilename()))){
            throw new RuntimeException("File already exists! Please enter another file name!");
        }
        String uploadedFile = fileService.uploadFile(path, file);

        //2. set the value of 'poster' as filename
        movieDto.setPoster(uploadedFile);

        //3. map dto to movie object
        Movie movie = new Movie(
                //we don't want this to perform update operation so made movieId as "null"
//                movieDto.getMovieId(),
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        //4. save the movie object -> saved move object
        Movie savedMovie = movieRepository.save(movie);

        //5. generate the posterurl
        String posterUrl = baseUrl + "/file/" + uploadedFile;

        //6. map movie object to dto object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        //1. check the data in the db and if exists ,fetch the data of given id
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie Not found exception"));

        //2. generate poster url
        String posterurl = baseUrl + "/file/" + movie.getPoster();

        //3. map to moviedto object and return it
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterurl
        );
        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        //1. fetch all data from the db
       List<Movie> movies = movieRepository.findAll();

       List<MovieDto> movieDtos = new ArrayList<>();
        //2. iterate through list , generate posterUrl for each movie object,
        //and map to movieDto object
        for(Movie movie:movies){

            //for generating poster url for each movie object
            String posterUrl = baseUrl +"/file/" + movie.getPoster();

            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        //1. Check if movie object exists with given movieId
        Movie mv = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Id not found"));

        //2. if file is null , do nothing
        // if file is not null, then delete existing file associated with the record
        //and upload the new file
        String fileName= mv.getPoster();

        if (file != null){
            Files.deleteIfExists(Paths.get(path+File.pathSeparator+fileName));
            fileService.uploadFile(path,file);
        }

        //3. set movieDto's poster value, according to step 2
        movieDto.setPoster(fileName);

        //4. map it to Movie object
        Movie movie= new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        //5. save the movie object -> return saved movie object
        Movie updatedMovie = movieRepository.save(movie);

        //6. generate poster url for it
        String posterUrl = baseUrl + "/file/" + fileName;

        //7. map to movieDto and return it
        MovieDto response= new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        //1. check if movie object exists in db
        Movie mv = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie id not found in db"));

        //2. delete the file associated with this object
        Files.deleteIfExists(Paths.get(path+File.pathSeparator+mv.getPoster()));
        //3. delete the movie object
        movieRepository.delete(mv);
        return "Movie deleted sucessfully";
    }
}
