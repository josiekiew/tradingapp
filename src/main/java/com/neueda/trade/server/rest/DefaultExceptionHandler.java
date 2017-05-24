package com.neueda.trade.server.rest;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception handling methods. Don't add try/catch within the MVC classes but report all
 * errors as exceptions and add an appropriate handler method to this class. Handlers return
 * and object that is automatically mapped to JSON or XML depending on the @Produces annotation type of the 
 * REST method being called.
 * 
 * Avoid returning underlying errors messages from Spring as these may contain information useful
 * for hackers attempting to break a system (table or column names, underlying technologies etc.).
 * 
 * For some Spring exceptions the contents of the message have to be examined to determine the actual
 * cause.
 * 
 * HTTP response codes should be used to indicate if the errors can be resolved by submitting the 
 * request with different data or whether the request can never be satisfied. There is no widely accepted
 * approach but this there is a good InfoQ article on 
 * <a href="https://www.infoq.com/articles/designing-restful-http-apps-roth">RESTful HTTP in practice</a>
 * that describes a widely used approach.
 * 
 *  @author Neueda
 *
 */
@ControllerAdvice
@Priority(100)
public class DefaultExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);
    
    /**
     * Lowest priority fallback exception handler
     * 
     * @param request
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> exceptionHandler(HttpServletRequest request, Exception ex) {
        logger.error(ex.toString(), ex);
        return new ResponseEntity<>(new FatalResponse(ex.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

   /**
    * Convenience class to define JSON/XML error response structure.
    *
    */
    public static class ErrorResponse {
        private Map<String,String> error = new HashMap<>();
        public ErrorResponse(String message) {
            this.error.put("message",message);
        }
        public Map<String, String> getError() {
            return error;
        }
    }

    /**
     * Convenience class to define JSON/XML fatal response structure.
     *
     */    
    public static class FatalResponse {
        private Map<String,String> fatal = new HashMap<>();
        public FatalResponse(String message) {
            this.fatal.put("message",message);
        }
        public Map<String, String> getFatal() {
            return fatal;
        }
    }
}
