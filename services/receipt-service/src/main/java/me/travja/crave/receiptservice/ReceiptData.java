package me.travja.crave.receiptservice;

import lombok.Getter;
import lombok.Setter;
import me.travja.crave.receiptservice.parser.ParserManager;
import me.travja.crave.receiptservice.parser.ProductInformation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReceiptData {

    @Getter
    private ReceiptType receiptType;

    @Getter
    @Setter
    private List<ProductInformation> productData;

    public ReceiptData(String data, ParserManager parserManager) {
        data = data.replaceAll("\n+", "\n");

        List<String> list = Arrays.stream(data.split("\n"))
                .filter(s -> s.length() > 2).collect(Collectors.toList());

        for (String str : list) {
            if (str.toLowerCase().contains("walmart"))
                receiptType = ReceiptType.WALMART;
            else if (str.toLowerCase().contains("target"))
                receiptType = ReceiptType.TARGET;

            if (receiptType != null)
                break;
        }

        if (receiptType == null)
            receiptType = ReceiptType.UNKNOWN;

        productData = parserManager.getParser(receiptType).parseData(list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Receipt Type: ").append(receiptType.toString()).append("\n\n");

        productData.forEach(dat -> sb.append(dat.toString()).append("\n"));

        return sb.toString();
    }

    public enum ReceiptType {
        WALMART,
        TARGET,
        UNKNOWN
    }


}