package org.project.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value="/index")
    public String home(){
        return "index";
    }

    @GetMapping(value="/vista")
    public String vista(){
        return "doc-main";
    }
}

