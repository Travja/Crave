package me.travja.crave.common.models.item;

import lombok.*;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.conf.Variables;
import me.travja.crave.common.util.Communication;
import me.travja.crave.common.util.Services;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SimpleReceiptData {

    private static LoadBalancerClient loadBalancerClient;
    private static RestTemplate       restTemplate;
    private static Variables          variables;

    static {
        loadBalancerClient = AppContext.getBean(LoadBalancerClient.class);
        restTemplate = AppContext.getBean(RestTemplate.class);
        variables = AppContext.getBean(Variables.class);
    }

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

    public boolean submit() {
        String auth = "Bearer " + variables.SERVICE_KEY;
        SubmissionResponse response = Communication.post(Services.ITEM_SERVICE, "/receipt", auth,
                this, SubmissionResponse.class);
        return response != null ? response.success : false;
    }

    public enum ReceiptType {
        WALMART,
        TARGET,
        UNKNOWN
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