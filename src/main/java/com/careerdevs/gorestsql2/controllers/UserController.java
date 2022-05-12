package com.careerdevs.gorestsql2.controllers;

import com.careerdevs.gorestsql2.models.User;
import com.careerdevs.gorestsql2.repository.UserRepository;
import com.careerdevs.gorestsql2.utils.ApiErrorHandling;
import com.careerdevs.gorestsql2.utils.BasicUtils;
import com.careerdevs.gorestsql2.validation.UserValidation;
import com.careerdevs.gorestsql2.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping ("/{id}")
    public ResponseEntity<?> getUserById (@PathVariable ("id") String id){
        try{
            if(BasicUtils.isStrNaN(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + "is not a valid ID");
            }
            int uID= Integer.parseInt(id);
            Optional<User> foundUser = userRepository.findById(uID);
            if(foundUser.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
            }
            return new ResponseEntity<>(foundUser, HttpStatus.OK);
        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage() , e.getStatusCode());
        }catch(Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable ("id") String id){
        try{
            if(BasicUtils.isStrNaN(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + "is not a valid ID");
            }
            int uID = Integer.parseInt(id);
            Optional<User> foundUser = userRepository.findById(uID);
            if(foundUser.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
            }
            userRepository.deleteById(uID);
            return new ResponseEntity<>(foundUser, HttpStatus.OK);
        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch(Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllUsers(){
        try{
            long totalUsers = userRepository.count();
            userRepository.deleteAll();

            return new ResponseEntity<>("Users deleted" + totalUsers, HttpStatus.OK);
        }catch(HttpClientErrorException e){
          return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch(Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    //use restTemplate only when need to make an external api request. Springboot makig a request to another api
    @PostMapping ("/upload/{id}")
    public ResponseEntity<?> uploadUserById(@PathVariable("id") String userId,
                                            RestTemplate restTemplate){
        try{
            if(BasicUtils.isStrNaN(userId)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST , userId + "is not a valid ID");
            }
            int uID = Integer.parseInt(userId);
            String url = "https://gorest.co.in/public/v2/users/" + uID;

            User foundUser = restTemplate.getForObject(url , User.class );

            if(foundUser == null){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, " User with id" + uID + "was not found");
            }

            //directly communicate with db
            User savedData = userRepository.save(foundUser);

            return new ResponseEntity<>(savedData, HttpStatus.CREATED);


        }catch(HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getClass());
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> createNewUser ( @RequestBody User newUser){
        try{
            ValidationError newUserErrors = UserValidation.validateNewUser(newUser);

            if(newUserErrors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST , newUserErrors.toString());
            }
            User savedUser = userRepository.save(newUser);
            return new ResponseEntity<>(savedUser , HttpStatus.CREATED);
        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
    @PutMapping("/")
    public ResponseEntity<?> updateNewUser( @RequestBody User updateUser){
        try{
            User savedUser = userRepository.save(updateUser);
            return new ResponseEntity<>(savedUser , HttpStatus.CREATED);
        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
    @PostMapping("/uploadAll")
    public ResponseEntity<?> uploadAll ( RestTemplate restTemplate){
        try{
            String url = "https://gorest.co.in/public/v2/users/";
            ResponseEntity<User[]> response = restTemplate.getForEntity(url , User[].class);

            User[] firstPageUsers = response.getBody();
            ArrayList<User> allUsers = new ArrayList<>(Arrays.asList(firstPageUsers));
            HttpHeaders responseHeaders = response.getHeaders();
            String totalPages = Objects.requireNonNull(responseHeaders.get("X-Pagination-Pages")).get(0);
            int totalPyNum = Integer.parseInt(totalPages);

            for(int i = 0 ; i < totalPyNum ; i++){
                String pageUrl = url + "?page" + i;
                User[] pageUsers = restTemplate.getForObject(pageUrl, User[].class);
                allUsers.addAll((Arrays.asList(firstPageUsers)));
            }
            userRepository.saveAll(allUsers);
            return new ResponseEntity<>("Users created" + allUsers.size(), HttpStatus.OK);


        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getClass());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(){
        try{
            Iterable<User> allUsers = userRepository.findAll();
            return new ResponseEntity<>(allUsers , HttpStatus.OK);

        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getClass());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
//where do SELECT queries fit in all of this?
//postman results ae diff