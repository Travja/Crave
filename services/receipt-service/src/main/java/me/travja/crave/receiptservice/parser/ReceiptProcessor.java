package me.travja.crave.receiptservice.parser;

import me.travja.crave.common.models.item.ProductInformation;
import me.travja.crave.common.models.store.Address;

import java.util.List;

public interface ReceiptProcessor {

    List<ProductInformation> parseData(List<String> list);
    Address getAddress(List<String> list);

}
