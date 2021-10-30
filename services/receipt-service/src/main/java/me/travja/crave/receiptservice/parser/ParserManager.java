package me.travja.crave.receiptservice.parser;

import lombok.Getter;
import me.travja.crave.common.models.item.ProductInformation;
import me.travja.crave.common.models.store.Address;
import me.travja.crave.receiptservice.models.ReceiptData;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Getter
public class ParserManager {

    private final WalmartParser walmartParser;
    private final TargetParser  targetParser;

    public ParserManager(WalmartParser walmartParser, TargetParser targetParser) {
        this.walmartParser = walmartParser;
        this.targetParser = targetParser;
    }

    public ReceiptProcessor getParser(ReceiptData.ReceiptType receiptType) {
        switch (receiptType) {
            case WALMART:
                return walmartParser;
            case TARGET:
                return targetParser;
            case UNKNOWN:
            default:
                return new ReceiptProcessor() {
                    @Override
                    public List<ProductInformation> parseData(List<String> list) {
                        return Collections.emptyList();
                    }

                    @Override
                    public Address getAddress(List<String> list) {
                        return null;
                    }
                };
        }
    }

}
