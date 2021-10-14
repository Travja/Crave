package me.travja.crave.receiptservice.parser;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WalmartParser implements ReceiptProcessor {
    private static Pattern pattern = Pattern.compile("^(.+?\\b)[ ]?(\\d{12}?\\b).*?(\\d+?\\.\\d{2})");

    @Override
    public List<ProductInformation> parseData(List<String> list) {
        //TODO Account for discounted items. Get titles as a way to verify UPC?

        List<ProductInformation> items = new ArrayList<>();

        int firstIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            String  str = list.get(i);
            Matcher mat = pattern.matcher(str);
            if (mat.find()) {
                firstIndex = i;
                String             name  = mat.group(1);
                String             upc   = mat.group(2);
                double             price = Double.parseDouble(mat.group(3));
                ProductInformation info  = new ProductInformation(name, upc, price);
                items.add(info);
            } else if (firstIndex != -1) {
                System.out.println("DOES NOT MATCH: " + str);
            }
        }

        if (firstIndex == -1 && items.isEmpty()) {
            System.err.println("No item found :O");
        }

        return items;
    }
}
