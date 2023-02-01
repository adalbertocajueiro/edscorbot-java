package es.us.edscorbot.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class SimpleController {

    @GetMapping(value="/")
    public ModelAndView index(){
        return new ModelAndView("forward:swagger-ui.html");
    }

    @PostMapping(value="/disconnect", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> disconnect(){
        String result = "";
        try{
            //it tries to establish connection with the arm
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
