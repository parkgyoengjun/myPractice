package com.example.practice3.testController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // json 이나 문자열 같은 데이터를 응답할 때 사용하면 좋음
public class TestController {

    @GetMapping("/api/hello")
    public String hello() {
        return "hello";
    }
}///
