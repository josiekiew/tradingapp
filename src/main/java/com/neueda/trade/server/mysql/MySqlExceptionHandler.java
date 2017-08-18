package com.neueda.trade.server.mysql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.neueda.trade.server.rest.DefaultExceptionHandler;

/**
 * Exception handling methods for MySQL. Avoid returning underlying errors messages from MySQL 
 * as these may contain information useful for hackers attempting to break a system 
 * (table or column names, underlying SQL statements etc.).
 * 
 * For MySQL exceptions the contents of the message have to be examined to determine the actual
 * cause see the {@link #dataIntegrityViolationExceptionHandler} for an example.
 * 
 *  @See com.neueda.trades.rest.DefaultExceptionHandler
 *  @author Neueda
 *
 */
@ControllerAdvice
@Priority(10)
public class MySqlExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MySqlExceptionHandler.class);


    Pattern nullColumn = Pattern.compile("Column '([^']+)' cannot be null");
    Pattern duplicateKey = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'");
    	
    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    public ResponseEntity<Object> dataIntegrityViolationExceptionHandler(HttpServletRequest request, DataIntegrityViolationException ex) {
        logger.warn(ex.toString());
        String msg = ex.getMessage();
        Matcher matchNullColumn = nullColumn.matcher(msg);
        Matcher matchDuplicateKey = duplicateKey.matcher(msg);
        if (msg.contains("FOREIGN KEY (`stock`)")) {
        	msg = "invalid stock ticker";
        }
        else if (msg.contains("FOREIGN KEY (`market`)")) {
        	msg = "invalid market ticker";
        }
        else if (matchNullColumn.find()) {
        	String column = matchNullColumn.group(1);
        	switch (column) {
        	case "buysell": column = "buy or sell"; break;
        	case "ptime": column = "placement time"; break;
        	case "stime": column = "state change time"; break;
        	}
        	msg = String.format("missing required value for %s", column);
        }
        else if (matchDuplicateKey.find()) {
        	String value = matchDuplicateKey.group(1);
        	String column = matchDuplicateKey.group(2);
        	switch (column) {
        	case "transid": column = "Transaction ID"; break;
        	}
        	msg = String.format("duplicate value %s for %s", value, column);
        }
        else {
        	msg = "unidentified constraint failure";
        }
        return new ResponseEntity<>(new DefaultExceptionHandler.ErrorResponse("Failed: "+msg), HttpStatus.NOT_ACCEPTABLE);
    } 
}
