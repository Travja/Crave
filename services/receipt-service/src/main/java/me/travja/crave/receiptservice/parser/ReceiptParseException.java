package me.travja.crave.receiptservice.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiptParseException extends RuntimeException {
    public String message;
}
