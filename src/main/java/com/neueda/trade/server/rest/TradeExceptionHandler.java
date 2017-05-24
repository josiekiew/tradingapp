package com.neueda.trade.server.rest;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.neueda.trade.server.FatalTradeException;
import com.neueda.trade.server.TradeException;

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
@Priority(20)
public class TradeExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(TradeExceptionHandler.class);
    
    @ExceptionHandler(value = {TradeException.class})
    public ResponseEntity<Object> tradeExceptionHandler(
            HttpServletRequest request, TradeException ex) {
        logger.warn(ex.toString());
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse(ex.getMessage()),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = {FatalTradeException.class})
    public ResponseEntity<Object> fatalTradeExceptionHandler(HttpServletRequest request, FatalTradeException ex) {
        logger.error(ex.toString(), ex);
        return new ResponseEntity<>(new DefaultExceptionHandler.FatalResponse(ex.getMessage()),HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        logger.warn(ex.toString());
        String error = ex.getMessage().split("\n")[0];
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse(error),HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        logger.warn(ex.toString());
        String error = ex.getMessage().split("\n")[0];
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse(error),HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        logger.warn(ex.toString());
        BindingResult result = ex.getBindingResult();
        FieldError error = result.getFieldError();
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse(error.getDefaultMessage()),HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Object> typeMismatchExceptionHandler(TypeMismatchException ex) {
        logger.warn(ex.toString());
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse("Incorrect format for request parameter: "+ex.getValue()),HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Object> numberFormatExceptionHandler(NumberFormatException ex) {
        logger.warn(ex.toString());
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse("Invalid number format: "+ex.getMessage()),HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = {DataAccessException.class})
    public ResponseEntity<Object> dataAccessExceptionHandler(HttpServletRequest request, DataAccessException ex) {
        logger.warn(ex.toString());
        return new ResponseEntity<>(new DefaultExceptionHandler.FatalResponse(ex.getClass().getSimpleName()),HttpStatus.SERVICE_UNAVAILABLE);
    }

}
