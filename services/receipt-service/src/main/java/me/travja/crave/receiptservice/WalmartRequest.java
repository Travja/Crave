package me.travja.crave.receiptservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.models.item.SimpleReceiptData;
import me.travja.crave.receiptservice.models.WalmartItem;
import me.travja.crave.receiptservice.parser.ParserManager;
import me.travja.crave.receiptservice.parser.WalmartParser;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class WalmartRequest {

    private String storeId, lastFourDigits, purchaseDate, cardType;
    private double total;

    public boolean submit() {
        ParserManager manager = AppContext.getBean(ParserManager.class);
        WalmartParser parser  = (WalmartParser) manager.getParser(SimpleReceiptData.ReceiptType.WALMART);

        WalmartParser.WalmartData dat   = parser.getItems(this, List.of(UUID.randomUUID().toString()));
        List<WalmartItem>         items = dat.getItems();
        SimpleReceiptData         data  = new SimpleReceiptData(SimpleReceiptData.ReceiptType.WALMART);
        if (dat.getAddress() != null) {
            data.setStreetAddress(dat.getAddress().getStreetAddress());
            data.setCity(dat.getAddress().getCity());
            data.setState(dat.getAddress().getState());
        }
        data.getProductData().addAll(items);
        return data.submit();
    }

}
