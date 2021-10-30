package me.travja.crave.receiptservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class ReceiptData extends SimpleReceiptData {

    private static LoadBalancerClient loadBalancerClient;
    private static RestTemplate       restTemplate;
    private static Variables          variables;

    static {
        loadBalancerClient = AppContext.getBean(LoadBalancerClient.class);
        restTemplate = AppContext.getBean(RestTemplate.class);
        variables = AppContext.getBean(Variables.class);
    }

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

        ReceiptProcessor processor = parserManager.getParser(receiptType);

        productData = processor.parseData(list);
        Address address = processor.getAddress(list);
        setStreetAddress(address.getStreetAddress());
        setCity(address.getCity());
        setState(address.getState());
    }

    public boolean submit() {
        String auth = "Bearer " + variables.SERVICE_KEY;
        SubmissionResponse response = Communication.post(Services.ITEM_SERVICE, "/receipt", auth,
                this, SubmissionResponse.class);
        return response != null ? response.success : false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionResponse {
        private boolean success;
        private int     updated;
        private String  error;
    }

}