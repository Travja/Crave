package me.travja.crave.common.models;

import lombok.*;

import java.util.List;

@Data
public class SimpleReceiptData {

    @Setter(AccessLevel.NONE)
    protected ReceiptType              receiptType;
    protected List<ProductInformation> productData;

    public enum ReceiptType {
        WALMART,
        TARGET,
        UNKNOWN
    }

}