package com.example.edusmile.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ErrorController {

    @GetMapping("/custom-500")
    public String custom500() {
        return "custom-500";
    }

    @GetMapping("/unknown_error")
    public String unknownError() {

        return "unknown_error";
    }
}
