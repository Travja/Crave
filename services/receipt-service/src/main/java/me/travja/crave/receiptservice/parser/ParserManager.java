package me.travja.crave.receiptservice.parser;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class ParserManager {

    @Getter
    private final WalmartParser walmartParser;
    @Getter
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
                return list -> Collections.emptyList();
        }
    }

}
