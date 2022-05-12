package com.careerdevs.gorestsql2.validation;

import com.careerdevs.gorestsql2.models.User;

public class CommentValidation {
    public static ValidationError validateNewUser(User user) {
        ValidationError errors = new ValidationError();

        if (user.getName() == null || user.getName().trim().equals("")){
            errors.addError("name" , "Name can not be left blank");
        }
        if (user.getEmail() == null || user.getEmail().trim().equals("")){
            errors.addError("email" , "Email can not be left blank");
        }
        if (user.getGender() == null || user.getGender().trim().equals("")){
            errors.addError("gender" , "Gender can not be left blank");
        }
        if (user.getStatus() == null || user.getStatus().trim().equals("")){
            errors.addError("status" , "Status can not be left blank");
        }
        return errors;
    }
}
