package com.careerdevs.gorestsql2.repository;

import com.careerdevs.gorestsql2.models.Todo;
import org.springframework.data.repository.CrudRepository;

public interface TodoRepository extends CrudRepository<Todo, Long> {
}
