package com.careerdevs.gorestsql2.validation;

import com.careerdevs.gorestsql2.models.Post;
import com.careerdevs.gorestsql2.models.User;
import com.careerdevs.gorestsql2.repository.PostRepository;
import com.careerdevs.gorestsql2.repository.UserRepository;

import java.util.Optional;

public class PostValidation {
    public static ValidationError validatePost(Post post, PostRepository postRepo, UserRepository userRepository, boolean isUpdating){

        ValidationError errors = new ValidationError();
        if(isUpdating){
            if(post.getId() == 0){
                errors.addError("id", "id cannot be left blank");
            }else {
                /*A container object which may or may not contain a non-null value.
                If a value is present, isPresent() will return true and get() will return the value.
                Additional methods that depend on the presence or absence of a contained value are provided,
                such as orElse() (return a default value if value not present) and ifPresent() (execute a block of code if the value is present).
                This is a value-based class; use of identity-sensitive operations (including reference equality (==), identity hash code,
                or synchronization) on instances of Optional may have unpredictable results and should be avoided.*/

                Optional<Post> foundUser = postRepo.findById(post.getId());
                if (foundUser.isEmpty()){
                    errors.addError("id", "No user found with the ID: "  + post.getId());

                }
            }
        }

        String postTitle =post.getTitle();
        String postBody = post.getBody();
        long postUserId = post.getUserId();

        if(postTitle == null || postTitle.trim().equals("")){
            errors.addError("title", "title can not be left blank");
        }
        if(postBody == null || postBody.trim().equals("")){
            errors.addError("body", "body can not be left blank");
        }
        if( postUserId == 0 ) {
            errors.addError("user_Id ", "user_Id can not be left blank");
        }else{

            Optional<User> foundUser = userRepository.findById(postUserId);
            if (foundUser.isEmpty()){
                errors.addError("user_id", "user_Id is invalid because there is no user found with the id:" + postUserId);
            }
        }
        return errors;
    }
}