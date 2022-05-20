package com.careerdevs.gorestsql2.controllers;

import com.careerdevs.gorestsql2.models.Comment;
import com.careerdevs.gorestsql2.models.Post;
import com.careerdevs.gorestsql2.repository.CommentRepository;
import com.careerdevs.gorestsql2.repository.PostRepository;
import com.careerdevs.gorestsql2.utils.ApiErrorHandling;
import com.careerdevs.gorestsql2.validation.CommentValidation;
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
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
     CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentById(@PathVariable("id") String id){
        try{
            if (ApiErrorHandling.isStrNan(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid id");

            } long uID = Integer.parseInt(id);

            Optional<Comment> foundComment = commentRepository.findById(uID);
            if(foundComment.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Comment with id is not found: " + id);

            }
            return new ResponseEntity<>(foundComment, HttpStatus.OK);

        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllComments(){
        try {
            Iterable<Comment> allComments = commentRepository.findAll();
            return new ResponseEntity<>(allComments, HttpStatus.OK);

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommentById(@PathVariable("id") String deleteComment){
        try{
            if(ApiErrorHandling.isStrNan(deleteComment)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, deleteComment +": is not a valid comment id");

            }
            long uID = Integer.parseInt(deleteComment);
            Optional<Comment> foundComment = commentRepository.findById(uID);

            if (foundComment.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "user id not found" + deleteComment);
            }
            commentRepository.deleteById(uID);
            return new ResponseEntity<>(foundComment, HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteALlComments (){
        try{
            long totalCommentPost = commentRepository.count();
            commentRepository.deleteAll();
            return new ResponseEntity<>("Total Comments deleted " + totalCommentPost , HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }


    @PostMapping("/upload/{id}")
    public ResponseEntity<?> postCommentById(@PathVariable("id") String commentId,
                                             RestTemplate restTemplate){
        try{
            if (ApiErrorHandling.isStrNan(commentId)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, commentId+ ": is not a valid comment id");


            }
            long cId = Long.parseLong(commentId);
            String url = "https://gorest.co.in/public/v2/comments/" + cId;
            Comment foundComment = restTemplate.getForObject(url, Comment.class);
            System.out.println("found comment");
            System.out.println(foundComment);

            if (foundComment == null){
                throw  new HttpClientErrorException(HttpStatus.NOT_FOUND, " data is null");
            }

            Iterable<Post> allPosts = postRepository.findAll();
            List<Post> result = new ArrayList<Post>();
            allPosts.forEach(result:: add);

            long randomPostId = result.get((int)(result.size() * Math.random())).getId();
            foundComment.setPost_id(randomPostId);

            Comment savedComment = commentRepository.save(foundComment);
            return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }
        catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @PostMapping("/uploadAll")
    public ResponseEntity uploadAll (RestTemplate restTemplate) {
        try {
            String url = "https://gorest.co.in/public/v2/comments/";

            ResponseEntity<Comment[]> response = restTemplate.getForEntity(url, Comment[].class);
            Comment[] firstPageComments = response.getBody();
            //

            assert  firstPageComments != null;
            if(firstPageComments == null){
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

            }
            ArrayList<Comment> allComments = new ArrayList<>(Arrays.asList(firstPageComments));
            HttpHeaders responseHeader = response.getHeaders();
            String totalPages = Objects.requireNonNull(responseHeader.get("X-Pagination-Pages").get(0));

            int totalPageNum = Integer.parseInt(totalPages);

            for( int i = 2; i<= totalPageNum; i++){
                String tempUrl = url + "?=page=" +i;
                Comment[] pageComment = restTemplate.getForObject(tempUrl, Comment[].class);
                if (pageComment == null){
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get page" + i+ "of comments");

                }
                allComments.addAll(Arrays.asList(pageComment));

            }

            Iterable<Post> allPosts = postRepository.findAll();
            List<Post> result = new ArrayList<Post>();
            allPosts.forEach(result:: add);

            for(int j=0; j<allComments.size(); j++){
                long randomPostId = result.get((int)(result.size() * Math.random())).getId();
                allComments.get(j).setPost_id(randomPostId);
            }

            commentRepository.saveAll(allComments);
            return new ResponseEntity("Comments added" + allComments.size(), HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
    @PostMapping("/addNew")
    public ResponseEntity<?> createNewComment(@RequestBody  Comment newComment){
        try {
            ValidationError error = CommentValidation.validateComment(newComment, commentRepository , postRepository,  false);
            if(error.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, error.toString());

            }
            Comment createComment = commentRepository.save(newComment);
            return new ResponseEntity<>(createComment, HttpStatus.CREATED);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updatingComment(@RequestBody Comment updateComment){
        try{
            ValidationError newCommentErrors = CommentValidation.validateComment(updateComment, commentRepository, postRepository, true);
            if(newCommentErrors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, newCommentErrors.toString());

            }
            Comment savedComment = commentRepository.save(updateComment);
            return new ResponseEntity<>(savedComment, HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
}
