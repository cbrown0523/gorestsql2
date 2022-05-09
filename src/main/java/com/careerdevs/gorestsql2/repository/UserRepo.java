package com.careerdevs.gorestsql2.repository;
import com.careerdevs.gorestsql2.models.User;
import org.springframework.data.repository.CrudRepository;
//repositories connects the models and controllers to the db
// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepo extends CrudRepository<User, Integer> {

}
