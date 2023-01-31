package es.us.robot.edscorbot.controllers;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import dto.ConnectDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Basic controller", description = "Routes related to the escorbot")
public class SimpleController {

    @GetMapping(value="/")
    public ModelAndView index(){
        return new ModelAndView("forward:swagger-ui.html");
    }

    @Operation(summary = "Get a book by its id",
    description = "Top meme's list (page) since 30 last days for a stream and given page number.")
    @ApiResponses(value = { 
        @ApiResponse (responseCode = "200", description = "Found the book", 
          content = { @Content(mediaType = "application/json", 
            schema = @Schema (implementation = String.class)) }),
        @ApiResponse(responseCode = "400", description = "Invalid id supplied", 
          content = @Content, links = @Link), 
        @ApiResponse(responseCode = "404", description = "Book not found", 
          content = @Content) })
    @GetMapping(value="/test", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getTest(@Parameter(description = "id of book to be searched", example = "gaming") String id){
        
        try{
            List<String> body = new LinkedList<String>();
            body.add("Test");
            return ResponseEntity.ok().body(body);
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/connect", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> connect(){
        ConnectDTO result = new ConnectDTO("Connection established!");
        try{
            //it tries to establish connection with the arm
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            result = new ConnectDTO("User cannot connect to the arm");
            return new ResponseEntity<String>(e.toString(),HttpStatus.CONFLICT);
        }
    }

    @PostMapping(value="/searchHomePosition", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> searchHomePosition(){
        ConnectDTO result = new ConnectDTO("Home position reached!");
        try{
            //it tries to invoke SearchHome function to position the arm
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            result = new ConnectDTO("User cannot connect to the arm");
            return new ResponseEntity<String>(e.toString(),HttpStatus.CONFLICT);
        }
    }

    @PostMapping(value="/disconnect", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> disconnect(){
        ConnectDTO result = new ConnectDTO("Connection finished!");
        try{
            //it tries to establish connection with the arm
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            result = new ConnectDTO("Some internal problem occurred during disconnection");
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
