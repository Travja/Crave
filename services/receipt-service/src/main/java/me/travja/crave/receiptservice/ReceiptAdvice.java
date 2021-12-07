package me.travja.crave.receiptservice;

import me.travja.crave.receiptservice.parser.ReceiptParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ReceiptAdvice {

    @ExceptionHandler(ReceiptParseException.class)
    protected ResponseEntity<Map<String, String>> noSuchElm(
            ReceiptParseException ex,
            WebRequest request) {

        Map<String, String> body = new HashMap<>();
        body.put("success", "false");
        body.put("error", "Receipt could not be parsed.");
        body.put("message", ex.getMessage());

        ResponseEntity<Map<String, String>> res = new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        return res;
    }

}
