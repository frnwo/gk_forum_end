package com.guangke.forum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AlphaController {
    @GetMapping("/index2")
    public String index(){
       return "redirect:index.html";
   }
    @GetMapping("/activationStatus")
    public String activation(){
       return "redirect:activation.html";
    }
}
