package com.movieflix.repositories;

import com.movieflix.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
}
