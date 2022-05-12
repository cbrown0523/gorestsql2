package com.careerdevs.gorestsql2.validation;

import com.careerdevs.gorestsql2.models.Todo;
import com.careerdevs.gorestsql2.models.User;
import com.careerdevs.gorestsql2.repository.TodoRepository;
import com.careerdevs.gorestsql2.repository.UserRepository;

import java.util.Optional;

public class TodoValidation {
    public static ValidationError validateTodo(Todo todo, TodoRepository todoRepo, UserRepository userRepository, boolean isUpdating){
        // need to validate data for post

        ValidationError errors =new ValidationError();
        if(isUpdating){
            if(todo.getId()==0){
                errors.addError("id", "id cannot be left blank");

            }else{
                Optional<Todo> foundTodo = todoRepo.findById(todo.getId());
                if(foundTodo.isEmpty()){
                    errors.addError("todo", "No todo is found with id:  " + todo.getId());

                }
            }
        }


        long userId = todo.getUserId();
        String title = todo.getTitle();
        String dueOn = todo.getDue_on();
        String status = todo.getStatus();


        if(title == null || title.trim().equals("")){
            errors.addError("title", "title cannot be left blank ");


        }
        if(dueOn == null || dueOn.trim().equals("")){
            errors.addError("due_on", "Due date cannot be left blank");

        }
        if (status== null || status.trim().equals("")){
            errors.addError("status", "Status cannot be left blank");

        }else if(!(status.equals("completed") || status.equals("pending"))){
            // this is not working ???/
            errors.addError("status", "status must be completed or pending");
        }
        if (userId == 0){
            errors.addError("user_Id", "user Id cannot be left blank");

        }else{

            Optional<User> foundUser = userRepository.findById(userId);
            if (foundUser.isEmpty()){
                errors.addError("user_Id", "User Id is invalid because there is not user found with the id: "+ userId);

            }
        }

        return errors;
    }
}
