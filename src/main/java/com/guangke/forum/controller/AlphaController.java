package com.guangke.forum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class AlphaController {
    @GetMapping("/index2")
    public String index(){
       return "redirect:index.html";
   }

    @GetMapping("/session")
    @ResponseBody
    public String session(HttpSession session){
        System.out.println(session.getAttribute("kaptcha"));
        return "session";
    }
}
