package com.careerdevs.gorestsql2.controllers;

import com.careerdevs.gorestsql2.models.Post;
import com.careerdevs.gorestsql2.models.User;
import com.careerdevs.gorestsql2.repository.PostRepository;
import com.careerdevs.gorestsql2.repository.UserRepository;
import com.careerdevs.gorestsql2.utils.ApiErrorHandling;
import com.careerdevs.gorestsql2.validation.PostValidation;
import com.careerdevs.gorestsql2.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/test")
    public String testRoute(){
        return "Testing!";
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById (@PathVariable("id") String id){
        try {
            if(ApiErrorHandling.isStrNan(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id +": is not a valid id");
            }
            long uID = Long.parseLong(id);

            //instead of it returning null
            Optional<Post> foundPost = postRepository.findById(uID);
            if(foundPost.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "post with id: "+ uID + " not found");
            }
            return new ResponseEntity<>(foundPost, HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        }catch (Exception e){

            return ApiErrorHandling.genericApiError(e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPosts(){

        try{

            Iterable<Post> allPosts = postRepository.findAll();
            return new ResponseEntity<>(allPosts, HttpStatus.OK);

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePostById (@PathVariable("id") String postId){
        try{
            if(ApiErrorHandling.isStrNan(postId)){ // checks if string is a number and if its null . if its not a number or null it will return true and throw exception.
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, postId + ": is not valid");
            }
            long uID = Long.parseLong(postId);
            Optional<Post> deletePost = postRepository.findById(uID);

            if (deletePost.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Post with id: "+ postId+ "was not found.");

            }
            postRepository.deleteById(uID);
            return new ResponseEntity<>(deletePost,HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllPosts(){
        try{
            long totalPosts = postRepository.count();
            postRepository.deleteAll();
            return new ResponseEntity<>("Posts deleted: "+ totalPosts , HttpStatus.OK );
        } catch (HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<?> postById (@PathVariable("id") String postId,
                                       RestTemplate restTemplate){
        try {
            if (ApiErrorHandling.isStrNan(postId)) { // check if post id is a number and is not empty. if it is throw Http Error
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, postId + " is not a valid id");

            }

            long uID = Long.parseLong(postId);

            String url = "https://gorest.co.in/public/v2/posts/" + uID;
            Post foundPost = restTemplate.getForObject(url, Post.class);

            System.out.println("found post");
            System.out.println(foundPost);

            if (foundPost == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, " post data was null");

            }

            Iterable<User> allUsers = userRepository.findAll();
            List<User> result = new ArrayList<User>();
            allUsers.forEach(result::add);

            long randomId= result.get((int) (result.size() * Math.random())).getId();

            foundPost.setUserId(randomId);

            // update post / SAVE POST
            Post savePost = postRepository.save(foundPost);

            return new ResponseEntity<>(savePost, HttpStatus.CREATED);

        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);


        }
    }


    @PostMapping("/uploadAll")
    public ResponseEntity uploadAll (RestTemplate restTemplate){
        try{
            String url = "https://gorest.co.in/public/v2/posts";

            ResponseEntity<Post[]> response = restTemplate.getForEntity(url, Post[].class);
            Post[] firstPagePost = response.getBody();
            assert firstPagePost != null;
            if(firstPagePost == null){
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, " Failed to get first page of go rest post");

            }

            ArrayList<Post> allPosts = new ArrayList<>(Arrays.asList(firstPagePost));

            HttpHeaders responseHeader = response.getHeaders();
            String totalPages = Objects.requireNonNull(responseHeader.get("X-Pagination-Pages").get(0));

            int totalPgNum =Integer.parseInt(totalPages);


            for( int i = 2; i<= totalPgNum; i++) {
                String tempUrl = url + "?=page=" + i;
                Post[] pagePost = restTemplate.getForObject(tempUrl, Post[].class);
                if (pagePost == null) {
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get page " + i + "of posts");
                }


                allPosts.addAll(Arrays.asList(pagePost));
            }
            // All users will be put in an array --- get all users to pick an id randomly
            Iterable<User> allUsers = userRepository.findAll();
            List<User> result = new ArrayList<User>();
            allUsers.forEach(result::add);

            for(int j=0; j< allPosts.size(); j++){


                long randomId= result.get((int) (result.size() * Math.random())).getId();

                allPosts.get(j).setUserId(randomId);
            }

            postRepository.saveAll(allPosts);

            return new ResponseEntity("posts added " + allPosts.size(),HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("//")
    public ResponseEntity<?> createPost(@RequestBody Post newPost){
        try{
            ValidationError errors = PostValidation.validatePost(newPost, postRepository, userRepository,false);
            if(errors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errors.toString());
            }
            Post createPost = postRepository.save(newPost);
            return new ResponseEntity<>(createPost, HttpStatus.CREATED);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());
        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updatingPost(@RequestBody Post updatePost){
        try{
            ValidationError newPostErrors = PostValidation.validatePost(updatePost,postRepository, userRepository,true );
            if(newPostErrors.hasError()){
                throw  new HttpClientErrorException(HttpStatus.BAD_REQUEST, newPostErrors.toString());

            }
            Post savedPost = postRepository.save(updatePost);
            return  new ResponseEntity<>(savedPost, HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

}

