package com.careerdevs.gorestsql2.controllers;

import com.careerdevs.gorestsql2.models.Todo;
import com.careerdevs.gorestsql2.models.User;
import com.careerdevs.gorestsql2.repository.TodoRepository;
import com.careerdevs.gorestsql2.repository.UserRepository;
import com.careerdevs.gorestsql2.utils.ApiErrorHandling;
import com.careerdevs.gorestsql2.validation.TodoValidation;
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
@RequestMapping("/api/todos")
public class TodoController {
    @Autowired
    TodoRepository todoRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/test")
    public String testRoute() {
        return "Hello !!!";
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getByIdFromSQL(@PathVariable("id") String id){
        try {
            if (ApiErrorHandling.isStrNan(id)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "post with id: "+ id + " not found" );

            }
            long uId = Long.parseLong(id);

            // optional is a container object which may or may not contain a null value. if a value is present .ispresent() returns true;
            //if the value is empty isEmpty() returns true

            Optional<Todo> foundTodo = todoRepository.findById(uId);
            if (foundTodo.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Post with id: " + uId + "not found");

            }
            return new ResponseEntity<>(foundTodo, HttpStatus.OK);
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);

        }

    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllFromSql(){
        try{
            //iterable - Implementing this interface allows an object to be the target of the enhanced for statement (sometimes called the "for-each loop" statement).
            Iterable<Todo> allTodos = todoRepository.findAll();
            return new ResponseEntity<>(allTodos, HttpStatus.OK);
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable("id") String todoId){
        try {
            if(ApiErrorHandling.isStrNan(todoId)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "invalid id");
            }
            long uId = Long.parseLong(todoId);
            //A container object which may or may not contain a non-null value. If a value is present, isPresent() returns true. If no value is present, the object is considered empty and isPresent() returns false.
            Optional<Todo> deleteTodo = todoRepository.findById(uId);

            if(deleteTodo.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND,"todo with id: "+ todoId+ " was not found.");
            }
            todoRepository.deleteById(uId);
            return new ResponseEntity<>(deleteTodo, HttpStatus.OK);

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteTodos(){
        try {
            long totalTodos = todoRepository.count();
            todoRepository.deleteAll();
            return new ResponseEntity<>("Todos deleted: "+ totalTodos, HttpStatus.OK);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/postNew")
    public ResponseEntity<?> addNewTodo(@RequestBody() Todo newTodo){
        try {
            ValidationError error = TodoValidation.validateTodo(newTodo, todoRepository, userRepository,false);
            if(error.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, error.toString());
            }
            Todo createTodo = todoRepository.save(newTodo);
            return new ResponseEntity<>(createTodo, HttpStatus.CREATED);

        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadByIdToDatabase(@PathVariable("id") String todoId, RestTemplate restTemplate){
        try {
            if (ApiErrorHandling.isStrNan(todoId)) { // error handling will check if todoId is a number if its null it will return true, if there is a number format exception it will return true. otherwise it will return false.

                //if apiErrorHandling is string not a number returns true throw error.
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, todoId + " is not a valid id");
            }
            long uID = Long.parseLong(todoId);

            String url = "https://gorest.co.in/public/v2/todos/" + uID;

            Todo foundTodo = restTemplate.getForObject(url, Todo.class);
            System.out.println(foundTodo);

            if (foundTodo == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, " Todo data was null");
            }

            // for each - add each user in an array.
            Iterable<User> allUsers = userRepository.findAll();
            List<User> result = new ArrayList<User>();
            allUsers.forEach(result::add);

            long randomId = result.get((int) (result.size() * Math.random())).getId();

            foundTodo.setUserId(randomId);

            Todo saveTodo = todoRepository.save(foundTodo);

            return new ResponseEntity<>(saveTodo , HttpStatus.CREATED);
        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/add/all")
    public ResponseEntity<?> addAllFromGoRest(RestTemplate restTemplate){
        try {
            String url = "https://gorest.co.in/public/v2/todos";
            ResponseEntity<Todo[]> response = restTemplate.getForEntity(url, Todo[].class);
            Todo[] firstPageTodo = response.getBody();
            assert firstPageTodo != null;

            if (firstPageTodo == null){
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to get first page of go rest Todos");

            }
            ArrayList<Todo> allTodo = new ArrayList<>(Arrays.asList(firstPageTodo));
            HttpHeaders responseHeader = response.getHeaders();

            String totalPages = Objects.requireNonNull(responseHeader.get("X-Pagination-Pages").get(0));

            int totalPgNum = Integer.parseInt(totalPages);

            for(int i = 2; i <= totalPgNum; i++){
                String tempUrl = url +"?=page=" + i;
                Todo[] pageTodo = restTemplate.getForObject(tempUrl, Todo[].class);

                if(pageTodo == null){
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get page " + i + "of todos");
                }
                allTodo.addAll(Arrays.asList(pageTodo));
            }
            Iterable<User> allUser = userRepository.findAll();
            List<User> result = new ArrayList<>();
            allUser.forEach(result::add);

            for (int j=0; j< allTodo.size(); j++){
                long radomId = result.get((int) (result.size() * Math.random())).getId();
                allTodo.get(j).setUserId(radomId);
            }

            todoRepository.saveAll(allTodo);
            return new ResponseEntity<>("todos added" + allTodo.size(), HttpStatus.OK);

        }catch (Exception e){
            ApiErrorHandling.genericApiError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updatingTodos(@RequestBody Todo updateTodo){
        try{
            ValidationError newTodoErrors = TodoValidation.validateTodo(updateTodo, todoRepository, userRepository , true);
            if(newTodoErrors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, newTodoErrors.toString());

            }
            Todo savedTodo = todoRepository.save(updateTodo);
            return new ResponseEntity<>(savedTodo, HttpStatus.OK);


        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());

        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }
}
