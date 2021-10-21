package me.travja.crave.receiptservice.parser;

import me.travja.crave.common.models.ProductInformation;

import java.util.List;

public interface ReceiptProcessor {

    List<ProductInformation> parseData(List<String> list);

}
