package me.travja.crave.receiptservice.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WalmartResponse {

    private List<WalmartReceipt> receipts = new ArrayList<>();

    @Data
    public static class WalmartReceipt {

        private WalmartStore store;
        private String       dateTime, tcNumber, barCodeImageUrl;
        private int                    noOfItems;
        private List<WalmartStoreItem> items = new ArrayList<>();

        @Data
        public static class WalmartStore {
            private WalmartAddress address;
            private String         phoneNumber, displayName;

            @Data
            public static class WalmartAddress {
                private String addressLineOne, addressLineTwo, city, postalCode, country;
            }
        }

        @Data
        public static class WalmartStoreItem {
            private String description, imageUrl, itemId, upc;
            private double price, unitPrice;
            private int    quantity;
        }

    }

}
