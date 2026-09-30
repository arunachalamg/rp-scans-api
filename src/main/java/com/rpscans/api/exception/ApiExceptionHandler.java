package com.rpscans.api.exception;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND) Map<String,Object> notFound(Exception e){return body(404,e.getMessage());}
 @ExceptionHandler({IllegalArgumentException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST) Map<String,Object> bad(Exception e){return body(400,e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) Map<String,Object> validation(MethodArgumentNotValidException e){String msg=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Validation failed");return body(400,msg);}
 private Map<String,Object> body(int status,String message){return Map.of("timestamp",Instant.now().toString(),"status",status,"message",message);}
}
