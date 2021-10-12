package me.travja.crave.receiptservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
public class TargetResponse {

    @Getter
    @Setter
    private TargetData data;

    @NoArgsConstructor
    public static class TargetData {
        @Getter
        @Setter
        private SearchData search;

        public class SearchData {

            @Getter
            @Setter
            private List<Product> products;

        }

        @NoArgsConstructor
        public static class Product {

            @Getter
            @Setter
            private Item item;

            @Getter
            @Setter
            private PriceInformation price;

            @NoArgsConstructor
            public static class Item {

                @Getter
                @Setter
                @JsonProperty("product_description")
                private ProductDescription productDescription;

                @NoArgsConstructor
                public static class ProductDescription {

                    @Getter
                    @Setter
                    private String title;

                }
            }

            @NoArgsConstructor
            public static class PriceInformation {
                @Getter
                @Setter
                @JsonProperty("current_retail")
                private double currentRetail;

                @Getter
                @Setter
                private String formattedCurrentPrice;
            }

        }

    }

}
