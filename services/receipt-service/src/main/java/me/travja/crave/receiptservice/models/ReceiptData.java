package me.travja.crave.receiptservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.conf.Variables;
import me.travja.crave.common.models.item.SimpleReceiptData;
import me.travja.crave.common.models.store.Address;
import me.travja.crave.common.util.Communication;
import me.travja.crave.common.util.Services;
import me.travja.crave.receiptservice.parser.ParserManager;
import me.travja.crave.receiptservice.parser.ReceiptProcessor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class ReceiptData extends SimpleReceiptData {

    public ReceiptData(String data, ParserManager parserManager) {
        data = data.replaceAll("\n+", "\n");

        List<String> list = Arrays.stream(data.split("\n"))
                .filter(s -> s.length() > 2).map(str -> str.replaceAll("[\\[\\]]", "1")).collect(Collectors.toList());

        log.info(String.join("\n", list));
        for (String str : list) {
            if (str.toLowerCase().contains("walmart") || str.toLowerCase().contains("live better"))
                receiptType = ReceiptType.WALMART;
            else if (str.toLowerCase().contains("target"))
                receiptType = ReceiptType.TARGET;

            if (receiptType != null)
                break;
        }

        if (receiptType == null)
            receiptType = ReceiptType.UNKNOWN;

        ReceiptProcessor processor = parserManager.getParser(receiptType);

        if (receiptType == ReceiptType.UNKNOWN)
            throw new IllegalArgumentException("Unknown receipt type.");

        productData = processor.parseData(list);
        Address address = processor.getAddress(list);
        setStreetAddress(address.getStreetAddress());
        setCity(address.getCity());
        setState(address.getState());
    }

}