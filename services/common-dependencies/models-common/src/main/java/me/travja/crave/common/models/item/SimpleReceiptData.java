package me.travja.crave.common.models.item;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SimpleReceiptData {

    @Setter(AccessLevel.NONE)
    protected ReceiptType receiptType;
    protected String      streetAddress, city, state;
    protected List<ProductInformation> productData = new ArrayList<>();

    public SimpleReceiptData(ReceiptType type) {
        this.receiptType = type;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress.toUpperCase().replaceAll("\\bAVE\\b", "AVENUE");
    }

    public enum ReceiptType {
        WALMART,
        TARGET,
        UNKNOWN
    }

}