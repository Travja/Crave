package me.travja.crave.receiptservice;

import lombok.Getter;
import lombok.Setter;

public class ReceiptData {

    @Getter
    @Setter
    private String data;

    public ReceiptData(String data) {
        this.data = data;
    }

}