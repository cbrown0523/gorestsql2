package com.careerdevs.gorestsql2.repository;

import com.careerdevs.gorestsql2.models.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long > {
        }
