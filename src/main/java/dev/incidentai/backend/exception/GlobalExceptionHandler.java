package dev.incidentai.backend.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiError> notFound(ResourceNotFoundException e,HttpServletRequest r){return error(HttpStatus.NOT_FOUND,e.getMessage(),r,null);}
 @ExceptionHandler(BusinessException.class) ResponseEntity<ApiError> business(BusinessException e,HttpServletRequest r){return error(HttpStatus.CONFLICT,e.getMessage(),r,null);}
 @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ApiError> unauthorized(BadCredentialsException e,HttpServletRequest r){return error(HttpStatus.UNAUTHORIZED,e.getMessage(),r,null);}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException e,HttpServletRequest r){var fields=new LinkedHashMap<String,String>();e.getBindingResult().getFieldErrors().forEach(f->fields.putIfAbsent(f.getField(),f.getDefaultMessage()));return error(HttpStatus.BAD_REQUEST,"Dados inválidos",r,fields);}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception e,HttpServletRequest r){return error(HttpStatus.INTERNAL_SERVER_ERROR,"Erro interno inesperado",r,null);}
 private ResponseEntity<ApiError> error(HttpStatus s,String m,HttpServletRequest r,java.util.Map<String,String> f){return ResponseEntity.status(s).body(new ApiError(LocalDateTime.now(),s.value(),s.getReasonPhrase(),m,r.getRequestURI(),f));}
}
