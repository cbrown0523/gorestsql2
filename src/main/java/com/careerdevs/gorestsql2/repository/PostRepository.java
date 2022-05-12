package com.careerdevs.gorestsql2.repository;

import com.careerdevs.gorestsql2.models.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long>{
}
