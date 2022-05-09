package com.careerdevs.gorestsql2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    public String getRootRoute(){
        return "Ypo are the root";
    }
}
