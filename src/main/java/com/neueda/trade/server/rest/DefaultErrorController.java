package com.neueda.trade.server.rest;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring uses a White Label error page to handle all errors. Rather than switch off this behaviour 
 * using the property {@code server.error.whitelabel.enabled=false} which allows the browser
 * to handle HTTP errors response codes this application defines its own error handling
 * controller. This allows more control over error handling and the ability to return JSON or XML
 * response objects.
 * 
 * @author Neueda
 *
 */
@RestController
public class DefaultErrorController implements ErrorController {

    private static final String PATH = "/error";

    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(value=PATH, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> errorText(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(status + " " + getMessage(request, status), status);
    }

    @RequestMapping(value=PATH)
    public ResponseEntity<Object> error(HttpServletRequest request,
                                        DefaultErrorAttributes errorAttributes) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> errors = errorAttributes.getErrorAttributes(requestAttributes, false);
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(new ErrorResponse(errors), status);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer status = (Integer)request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            try { return HttpStatus.valueOf(status); }
            catch (Exception ex) {}
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getMessage(HttpServletRequest request, HttpStatus status) {
        String msg =  (String)request.getAttribute("javax.servlet.error.message");
        if (msg == null || msg.trim().equals("")) {
            msg = status.getReasonPhrase();
        }
        return msg;
    }

    public static class ErrorResponse {
        private Map<String,Object> error = new HashMap<>();
        public ErrorResponse(Map<String,Object> items) {
            this.error.putAll(items);
        }
        public Map<String, Object> getError() {
            return error;
        }
    }
}