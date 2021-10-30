package me.travja.crave.receiptservice.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TargetResponse {

    private TargetData data;

    @Data
    public static class TargetData {
        private SearchData search;

        @Data
        public static class Product {

            private Item             item;
            private PriceInformation price;
            private String           tcin;

            public double getRetailPrice() {
                return price.getCurrentRetail();
            }

            @Data
            public static class Item {

                @JsonProperty("product_description")
                private ProductDescription productDescription;
                private Enrichment         enrichment;

                @Data
                public static class ProductDescription {
                    private String       title;
                    @JsonProperty("soft_bullets")
                    private SoftBullets  softBullets;
                    @JsonProperty("bullet_descriptions")
                    private List<String> bulletDescriptions = new ArrayList<>();

                    public String getDescription() {
                        StringBuilder sb = new StringBuilder();
                        getBulletDescriptions().forEach(str -> sb.append(str + "\n"));
                        sb.append("\n\n");
                        getSoftBullets().getBullets().forEach(str -> sb.append(str + "\n"));
                        sb.substring(0, sb.length() - 1);
                        return sb.toString();
                    }

                    @Data
                    public static class SoftBullets {
                        private List<String> bullets = new ArrayList<>();
                    }
                }

                @Data
                public static class Enrichment {
                    private Map<String, Object> images = new HashMap<>();

                    public String getPrimaryImageUrl() {
                        images.keySet().forEach(img -> System.out.println(img + ". " + images.get(img)));
                        return (String) images.get("primary_image_url");
                    }

                    public List<String> getAlternateImages() {
                        return (List<String>) images.get("alternate_image_urls");
                    }
                }
            }

            @Data
            public static class PriceInformation {
                @JsonProperty("current_retail")
                private double currentRetail;

                private String formattedCurrentPrice;
            }

        }

        @Data
        public class SearchData {
            private List<Product> products;
        }

    }

}
