package com.careerdevs.gorestsql2.validation;

import java.util.HashMap;
import java.util.Map;

public class ValidationError {
    private final HashMap<String , String> errors = new HashMap<>();

    public void addError ( String key, String errorMsg){
       errors.put(key, errorMsg);
    }
    public boolean hasError(){
        return errors.size() !=0;
    }
    @Override
    public String toString(){
        String errorMessage = "Validation Error: \n";
        for(Map.Entry<String , String> err : errors.entrySet()){
            errorMessage += err.getKey() + ":" + err.getValue();
        }
        return errorMessage.toString();
    }
}
