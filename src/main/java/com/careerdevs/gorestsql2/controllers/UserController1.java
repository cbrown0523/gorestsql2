package com.careerdevs.gorestsql2.controllers;

import com.careerdevs.gorestsql2.models.User;
import com.careerdevs.gorestsql2.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/user")
public class UserController1 {
    @Autowired
    private UserRepo userRepository;
    @GetMapping("/upload/{id}")
    public ResponseEntity<?> uploadUserById(@PathVariable("id") String userId,
                                            RestTemplate restTemplate){
        try{
            int uID = Integer.parseInt(userId);
            String url = "https://gorest.co.in/public/v2/users/" + uID;

            User foundUser = restTemplate.getForObject(url , User.class );
            System.out.println(foundUser);

            return new ResponseEntity<>("Temp", HttpStatus.OK);


        }catch(NumberFormatException e) {
            return new ResponseEntity<>("Id must be a number" , HttpStatus.NOT_FOUND);
        }catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getClass());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
