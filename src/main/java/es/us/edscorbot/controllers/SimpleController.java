package es.us.edscorbot.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin(origins = "/**")
@RestController
public class SimpleController {

    @GetMapping(value="/")
    public ModelAndView index(){
        return new ModelAndView("forward:swagger-ui.html");
    }
}
