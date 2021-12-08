package me.travja.crave.receiptservice.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.travja.crave.common.models.item.ProductInformation;
import me.travja.crave.common.models.store.Address;
import me.travja.crave.receiptservice.WalmartRequest;
import me.travja.crave.receiptservice.models.WalmartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WalmartParser implements ReceiptProcessor {

    private static Map<List<String>, Address> addressCache = new HashMap<>();

    private static Pattern lastFourPattern = Pattern.compile("(\\d{4})");
    private static Pattern datePattern     = Pattern.compile("(\\d{1,2}\\/\\d{1,2}\\/\\d{1,4})");
    //    private static Pattern totalPattern    = Pattern.compile("\\d+?\\.\\d{2}");
    private static Pattern pattern         = Pattern.compile("^(.+?\\b)[ ]?(\\d{12}?\\b).*?(\\d+?\\.\\d{2})");

    private static DateFormat receiptDateFormat = new SimpleDateFormat("MM/dd/yy");
    private static DateFormat finalDateFormat   = new SimpleDateFormat("MM-dd-yyyy");

    private static List<String> cardTypes = new ArrayList<>(Arrays.asList("discover", "visa", "mastercard", "amex", "debit"));
    private final  RestTemplate restTemplate;
    private        Logger       log       = LoggerFactory.getLogger(WalmartParser.class);

    public WalmartParser(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

    public static String getValidatedUPC(String upc) {
        upc = upc.substring(1);

        int odd = 0, even = 0;
        for (int i = 0; i < upc.length(); i++) {
            int num = Integer.parseInt(String.valueOf(upc.charAt(i)));

            if (i % 2 == 0) odd += num;
            else even += num;
        }
        odd *= 3;

        int checkDigit = 10 - (odd + even) % 10;
        if (checkDigit == 10) checkDigit = 0;

        return upc + checkDigit;
    }

    @Override
    public List<ProductInformation> parseData(List<String> list) {
        //TODO Account for discounted items. Get titles as a way to verify UPC?

        List<ProductInformation> items = new ArrayList<>();
        List<String>             upcs  = new ArrayList<>();

        String storeId = "",
                lastFourDigits = "",
                purchaseDate = "",
                cardType = "";
        double total = -1;

        int firstIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            String  str = list.get(i);
            Matcher mat = pattern.matcher(str);
            if (mat.find()) {
                firstIndex = i;
                String name  = mat.group(1);
                String upc   = mat.group(2);
                double price = Double.parseDouble(mat.group(3));
                //TODO Get description;
                ProductInformation info = new ProductInformation(name, upc, null, null, price);
                items.add(info);
                upcs.add(upc);
            } else {
                log.info(str);
                if (str.toLowerCase().startsWith("st")) {
                    storeId = str.split(" ")[1];
                    log.info("Store Id: " + storeId);
                } else if ((str.contains("xx") || str.contains("**") || str.toLowerCase().contains("debit"))
                        && lastFourPattern.matcher(str).find()) {
                    Matcher mt = lastFourPattern.matcher(str);
                    mt.find();
                    lastFourDigits = mt.group();
                    log.info("Last Four Digits: " + lastFourDigits);

                    if (str.toLowerCase().contains("debit")) {
                        cardType = "debit";
                    } else if (str.toLowerCase().contains("credit") || str.toLowerCase().contains("card")) {
                        String type = str.split(" ")[0].toLowerCase();
                        if (cardTypes.contains(type))
                            cardType = type;
                        else
                            cardType = "other";
                    } else {
                        cardType = "No Card Type";
                    }
                    log.info("Card type: " + cardType);
                } else if (datePattern.matcher(str).find()) {
                    try {
                        Matcher mt = datePattern.matcher(str);
                        mt.find();
                        Date date = receiptDateFormat.parse(mt.group());
                        log.info(date.toString());
                        purchaseDate = finalDateFormat.format(date);
                        log.info("Purchase Date: " + purchaseDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (str.startsWith("TOTAL")) {
                    log.info(str);
                    String totalStr = str.split(" ")[1];//totalPattern.matcher(str).group();
                    total = Double.parseDouble(totalStr);
                    log.info("Total: " + total);
                }
            }
        }

        log.info("Store Id: " + storeId + " -- Last Four: " + lastFourDigits + " -- Card Type: " + cardType + " -- " +
                "Purchase Date: " + purchaseDate + " -- Total: " + total);

        List<WalmartItem> fetchedItems =
                getItems(new WalmartRequest(storeId, lastFourDigits, purchaseDate, cardType, total), list).getItems();

        fetchedItems.forEach(System.out::println);

        if (firstIndex == -1 && items.isEmpty()) {
            log.warn("No item found :O");
        }

//        for (WalmartItem fItem : fetchedItems) {
//            if (upcs.contains(fItem.getUpc())) {

//                items.stream().filter(itm -> itm.getUpc().equals(fItem.getUpc()))
//                        .findFirst().ifPresent((itm) -> fItem.setPrice(itm.getPrice()));

//            }
//        }

        return fetchedItems.stream().collect(Collectors.toList());
    }

    @Override
    public Address getAddress(List<String> list) {
        if (addressCache.containsKey(list)) return addressCache.get(list);

        return null;
    }

    public WalmartData getItems(WalmartRequest req, List<String> list) throws IllegalArgumentException {
        if (req.getStoreId().isEmpty() || req.getLastFourDigits().isEmpty() || req.getPurchaseDate().isEmpty()
                || req.getCardType().isEmpty() || req.getTotal() == 0)
            throw new ReceiptParseException("Invalid data supplied.");

        if (req.getLastFourDigits().length() != 4)
            throw new ReceiptParseException("Card number not 4 digits");

        Address address = null;
        String  url     = "https://www.walmart.com/chcwebapp/api/receipts";

        WalmartResponse response = restTemplate.postForObject(url, req, WalmartResponse.class);

        WalmartResponse.WalmartReceipt.WalmartStore.WalmartAddress walAddress = response.getReceipts().size() > 0 ?
                response.getReceipts().get(0).getStore().getAddress() : null;
        log.info("Address: " + (walAddress != null ? walAddress.toString() : "No address"));
        if (list != null && walAddress != null) {
            address = new Address();
            address.setStreetAddress(walAddress.getAddressLineOne());
            Pattern statePat = Pattern.compile("\\b\\w{2}\\b");
            Matcher mat      = statePat.matcher(walAddress.getAddressLineTwo());
            if (mat.find())
                address.setState(mat.group());

            address.setCity(walAddress.getCity());
            addressCache.put(list, address);

            log.info(address.toString());
        }

        List<WalmartResponse.WalmartReceipt> receipts = response.getReceipts();
        List<WalmartItem>                    items    = new ArrayList<>();

        receipts.forEach(receipt -> {
            log.info(receipt.toString());
            receipt.getItems().forEach(item -> {
                WalmartItem wItem = new WalmartItem();
                wItem.setName(item.getDescription());
                if (item.getImageUrl() != null-)
                    wItem.setImage(item.getImageUrl().replace("odnHeight=180", "odnHeight=420").replace("odnWidth=180",
                            "odnWidth=420"));
                wItem.setPrice(item.getUnitPrice() > 0d ? item.getUnitPrice() : item.getPrice());
                wItem.setUpc(getValidatedUPC(item.getUpc()));

                //TODO Get description information.
                items.add(wItem);
            });
        });

        return new WalmartData(items, address);
    }

    @Data
    @AllArgsConstructor
    public static class WalmartData {
        private List<WalmartItem> items = new ArrayList<>();
        private Address           address;
    }

    /**
     * Can fetch data from Walmart's Receipt API using the applicable information scraped from the receipt.
     *
     * POST https://www.walmart.com/chcwebapp/api/receipts
     * {
     *   "storeId": "3589",
     *   "purchaseDate": "10-26-2021",
     *   "cardType": "discover",
     *   "total": "31.32",
     *   "lastFourDigits": "3181"
     * }
     *
     *
     *
     * RESPONSE
     * {
     *     "receipts": [
     *         {
     *             "store": {
     *                 "address": {
     *                     "addressLineOne": "350 HOPE AVE",
     *                     "addressLineTwo": "SALT LAKE CITY UT 84115",
     *                     "city": "Salt Lake City",
     *                     "postalCode": "84115",
     *                     "country": "US"
     *                 },
     *                 "phoneNumber": "801-484-7311",
     *                 "displayName": "Salt Lake City Supercenter"
     *             },
     *             "dateTime": "10-26-21 15:43:57",
     *             "noOfItems": 9,
     *             "total": {
     *                 "subtotal": 30.27,
     *                 "taxTotal": 1.05,
     *                 "totalAmount": 31.32,
     *                 "changeDue": 0
     *             },
     *             "tcNumber": "58012485240842701043",
     *             "barCodeImageUrl": "https://receipts-query.edge.walmart.com/barcode?barWidth=2&barHeight=50&data=GCTTOY5R6YACFLYF",
     *             "items": [
     *                 {
     *                     "description": "Bar S Original Premium Beef Franks, 12 Oz.",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/7cc3c6a7-aa2a-4d15-b9bf-d1e5dfe0a8d1.e279628e81b8488f83ba52671d55306b.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0551222861",
     *                     "upc": "001590000094",
     *                     "price": 2.68,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "Great Value Large White Eggs, 6 Count",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/e6d1bcd9-d514-4302-a504-ad2727c42a71_1.0284676ccd21c5445a6ed45e58dd4a89.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0555727867",
     *                     "upc": "007874212709",
     *                     "price": 1,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "Bananas, each",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/209bb8a0-30ab-46be-b38d-58c2feb93e4a_1.1a15fb5bcbecbadd4a45822a11bf6257.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0557969037",
     *                     "upc": "064312604011",
     *                     "price": 0.96,
     *                     "unitPrice": 0.54,
     *                     "quantity": 1.78
     *                 },
     *                 {
     *                     "description": "Great Value Hot Dog Buns, White, 11 oz, 8 Count",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/9fdc598c-a2d5-4316-9f08-94a91a505baa.c2b701e639ff82fce713324c99f965f6.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0571614377",
     *                     "upc": "007874209728",
     *                     "price": 0.88,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "CLIF BAR® Energy Bars, Chocolate Brownie, 9g Protein Bar, 12 Ct, 2.4 oz (Packaging May Vary)",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/4c7953ec-e3a1-487d-b11a-e94e48f0a2a4.ca24efb78b0d9d61f80aa030b35afc2c.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0565694271",
     *                     "upc": "072225231324",
     *                     "price": 11.28,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "Mossy Oak Blaze Orange Insulated Beanie, One Size Fits Most",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/ddc2abf9-8cdd-4def-9996-0f9d1213a4d1.4eb6f761ae533c96db824d90c8d94b4c.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0580727929",
     *                     "upc": "084033818824",
     *                     "price": 2.97,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "Freshness Guaranteed Kosher Cinnamon Rolls, 22 Oz",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/b635eb85-d292-404f-9a1b-cd02cc0a45a6_1.b932ca00eb7366a95cd58d68c76cc2d5.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0554521899",
     *                     "upc": "007874212129",
     *                     "price": 2.63,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "Malt-O-Meal Golden Puffs, 34.5 oz",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/5cfb5e1d-9968-4d8e-9896-e73da22468bc_1.a5d5a878fb21166bd9fccaa889f89a22.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0568301022",
     *                     "upc": "004240029396",
     *                     "price": 4.98,
     *                     "quantity": 1
     *                 },
     *                 {
     *                     "description": "Great Value 2% Reduced Fat Milk, Gallon, 128 fl oz",
     *                     "imageUrl": "https://i5.walmartimages.com/asr/cbcc26fb-af25-4672-bfb8-cb156cf1dadf_2.b93305e90bf7f66b9ab5f36f25652660.jpeg?odnHeight=180&odnWidth=180&odnBg=ffffff",
     *                     "itemId": "0555063965",
     *                     "upc": "007874235187",
     *                     "price": 2.89,
     *                     "quantity": 1
     *                 }
     *             ],
     *             "tender": [
     *                 {
     *                     "name": "OTHER",
     *                     "amount": 31.32
     *                 }
     *             ],
     *             "image": "iVBORw0KGgoAAAANSUhEUgAAAioAAAQ3CAAAAADzwxoQAACAAElEQVR42uydCZgUxfnGv509Zg9uVlluBJRj0QVRkYiiEgRFRcVoFAQBRSMSiIjEEy9E451o0Hj9ve+gUROIIgY8EZVLEVBALkGUFUHYhd2tf1ef9VV39fTM9OzOLN/7PMr2VHd1Tfc73dXVv34bGIkUSECbgERWqb96/+IuUSg4fPKXZBWSn34cAqay/lBJViEptbMHODqthqxCUulWEPUaWYWk0vHIKpeTVUjBrHIZWYWk0rnIKreQVUgqXY6s8hBZhaTSdOrWkoLpH8gqH5FVSCq9gazyLVmFpNLHyCq/klVIKq0TnVLIyCoklfaIVulMViGp1UCwSl+yCkmtgwWrnEFWIan1G8Eql5BVSGoNE6xyA1mFpNalglUeJKuQ1LpRsMpLZBWSWjMFq7yf7laZJ+oLjxm+EGfw+Do/ieWLEl73hrTfrauF1i4LssDuxSuqY8zyimCVlTHmrVjyZVWdWuUgcRSonccMrcQZsn52lf9dLB8RX4sF3Zv2VpkotHZo7Nm3jS8C6Phv/5k+EOrc4TtnzUPNAQ64eU8dWmUUug3xk6t8DSqH//pX8CRZxdQi8yd2qe+dnVVOlVHf6r4/xZir54a6s8oTMZzwJLbKra4ZuonFm8gqhlY0t2Y9+GOf2Xb6H9GdE5VdXdef6swqa5ETZrjKL8ZWGSKX/5IllHZnZBVde3s682Zfv1c9Y74921HqmXZcIKz63Lq7Amon7rCzXcVdsVUOkMvniqV/JKu4r2wAeq9QztjBnukUde8f7aIwrpQStMpwsRkd5dIfQNIaaYYZYuG/yCqGDsYbLf+vqsfB+tjzXKSYY88VWbiyYXVmlUdQO+QrnFmyVZ6TZjhLKMv+hayi6yt5q8FvN3rPeao9x9XeM3xRKleVX1lXVlmN2jFXKp0sN3SiNENroawfI6voesZlFWjynOecTlfwr17FVTPy3FUtqSuroJ0Nf5EKj5LbeTQu3ySW3UhWMXQveOhcr2uXa+zi5z1Kvz3Gq6J5dWYV9NyS1L/+NVtuZ7RSfYJ6n6xi6EWvPQyt5rjn/Jtd+q5H36DIs54v68wqqLd+iM/1jaFP0AxXCyUN95FVDG3O9tzHcLlrPG6BVZT7g1y09VTvSlpW1ZlVVqCRe9wzvdHd0r+hGX4rpj4wsorHvKK6LJTnHGeW3CcXvFasqOMfdXYFxFgLsSH/UzrB1HCxvKaRUHI/WcXujl7nfe6A7BvlQ+9Lv20CJWctkEfdRiuMcmAITkncKr8Tm3K3WLLP4wt3Vh6RXOfQHV99sWDewiXfh2CVXd8umrdEccG5bcVH7y2JPd69d8MXC+ct+GJlfHfcflo2b+H6uK2iLXfrgd77+siVAdb6vw7eC3d+aDerS6v8TWzM+WLJQqdL5szxozDDU2KvTTTJ7BvPLi2wu8LdRz28KahVFji3+s2fWtVbo7oaw1DNT35CGrr5+fEzzKa1PP8V1YBDzZcPXXKss+daDLx+vgcdsNrNGKy+0rjD9YTPSWWaarPufqiz5wIFD8aKZ6q4KstzyaNeqQ7nAJmwVZaB6jbOPfbHk5w5xPvqlwlLjrI+XDX9NxHX18w67tXqQFZp7HzWWD+yPdxSnK/RDOF3tXl8vljW9mGPTbnn5ZHNPfqGUzf7nGCMg8YW+9ZLQlZhrPqVozwXGbTRd38sPcxzqSH/C+1cmrBVasQOVGSX51DsPG9c+Ehhyaf1Tyqf+Y3iLAs93k/AKmuOkKsp/draE3cVyGXHyAevry5tpGhN9No9/lZ5rQkkaRV+JjnFa5mD/U4ja71anDtqeYjdrsSByTNVgyMHWB82rxB+E8KRUhxL1LskT7cFH11dHa9VPm7irqWRcVt/+4kea2ixVFzB+rP8WtN1pZ9V/iEcGRO3CmPLR+W6F3rVZ4Gb3LM3vDJcSDBxq6DBReE65mv7w+OFMd2mzqn2E/GYoX9yPvjqrKr4rPKF5zGh8Sptvg1dva8Q1os3UPxb0/hjtVVeE8+hyVhFa+nkhvJCfp34i+WZS24vD/liLnGroC06UhgtFOiC/s4cX9szPCAsN0n/5Dn/neN0aAJZ5cf2iquIfeyHboo1HCFejbbxb02Tr1RWWYf2bnJWYaz89hK80EvBjypdHw0/0DZxq1Q3FZp2qPO5w0I+I3Zgn7JnEJGbN42zQiSGV2bGYZVGZytr2ddPuQYRz7okRmtKdyusMgTCtIrWhXtEPAjmb/eZ9RPU+Xq9mrE0sgo7XRwkcrZeJ/vDFeLwv5OE2EXod5nd4eNi7JyiTcGtolbbiT5r2Oas4Y1YFV3jbZX5ELJVtN/j605//0rfOQfaV41DP0jNaGISVrnTM2fqe6dbVc3+J0Bd9qCGsNRx5md3OKePXkPOG3V673xp044Jwyq+ulYY27DXHul0wpmjzj1R7nXnb/S0ytDwraLp/aHGeMlR/uNoG41+Yd5FK1I18JyEVT4Vv/sD7vujJ2qnW2eGHOub/ldY6marw290Lsc8v9YaJP3vafiy7/tUW6WV0HfWTySRE2Z8aLV53a14lOXPXlYpyUmJVRhbMTYXskbuiDHXdwO0H9qfN6fuHkUSVqlq5PWzn4B+p8IDQ9ZhUQxI/NBaqgNkn/MOPsG+hsY/7orTKmUPLN226T+jvW7WZp/98rfl616VjgHwjrNq7bx5yD1bUWt+7I9g4apYt/jCtIq29veCWGD1gl2pvJ2VzIOoYi+ul/VhT/TMx+/c94mEXdTI3uJ/vHCtq/pZ3nBUEKvk/dW03WetXXupm/U05Fv4mlroCqzv9IKrX/grGg19188qWYeePGLYoC7PyuVtR9maxTJPyVhFhKnzzGcRdtoXM9k7tcm/uPmnVl73z/b59tT46eDnOKyS+7ozytNAPtw4twjfRhdevcU7nh6tEU+ccJ3aKof/388J3lmux1b5SNxCi43P3nGGMfjkXNfN5e+VFItLaLhldhxWuc/zjpQRs7dGdVUcjcFY1YjDLQNUVil4oEY1mrs/WwXRBv9nfHaL/cFkPik+G2b82N4M/mz2SnEf3BPcKkeKe2s3Ps0gkhdlNQqDhLHvZJQorNLwI/XtxP3ZKmyQG8ofLCV6d5G5fmFYsW2s4UrPN1TEtsobqJYRqBuBO6vijUuYE6M5aOx8l7dVnmdkFU9Ndw2R1Ng36rJ+kneUwfULF8GjvWvdvuDZ6ZcPP6V//8M8H2GMaZXmGCNFz093w6u63nXF4j50fvnG36666KwT+vdFLOI3nlY5nZFVvLVAvHOrf+I89lRqzHK/3K8VerXPumtcfO8Zijsw/QNbZSCu8jMlg4WTSjzuxv308vjDo97N+cLTKgvJKqp7FOLQx7f4XuEfjFk+kPq1Yq92q1TdV1d0VI9SBLfKhdL+FheYgMuW+jzNtPuJQTnq5vzPyyodGVlFpQEyTTFGPmvvzsH9WqFXexiua/5A39HUssBWkR5lrBQXkAIZNymHxcqvaebbnHleVrmErKLUTfJQQ1fMLOGe41y8xBVoWPq0GAPvCVuF+YySlivKah5oFqM5nla5j6yilEBEwqnoYN/FPdCvH+FP8xgp0fR8zJs4KbGKomzrgJi3jDyt8kqqrPLVU5/Gnqn63Wc3pK9V9kTxpe9b7mOxMI72eyY+65wnPDR3S+zbebVolRUdIDGrvJYaq8zlIxD9Y2Vbfa4d0XNGLE1Xq4iYG++K3OAeYFiLuP5tIOCUtm6HdLLK+taQoFXmpcAqVS+aSHln/4eWVphDjYPnpqlVbsAo9smurgpjzqM02ZXsbWd2JyDuNTeEdMbE2x9/adYJdWGVCtdTEvl9xl7/96dn3VMHVtn9QEcvZsfjtkNf52bWi1XpaJW3MdVY7OqqoDvJX4i3D+0n3n9qge/LDn7KuooeVRdWuRb7pPW1n5o3h76odatsu0nEZLK3Buw1QscHdqefVX4VnkEYz771umwUzi5PCmx+E9v6U9GuOV54dKUurLIZAXgN/+bgzLVtlTXjpSeWnvOZ+To8a/Mbt6WbVdgx4iDZi173QgRocgo71HliwyrfiR5x/qMIitSFVa4SP2oj3kKsXassOseFZd3nM/tF8swF479NM6sIh+umQpqQ8HC6EMwzdJ9zEPq7fZ0sfsHT0C38OrBKjcjR5ixmdWOVmtleT7a94bPEDA/a75xFaWWV/wht23S6xKYY6u08nSJkIKy2ilFYJcYSRta+VZaIn4xjoVrltwG36b6nPZ8/LvMDarZ4RiicOLsmfayyUzhMzrbHI8aKs/zBodOcM1R7u7i7F3bp6hHXllX+T/wEx3x/kKxVfhNsi97j/VhuDA77uxM8lzr06X3pYhUhQxWm2X89Lc7xtPCYILjNlK2+8XtE7VsF9bHxqNdLyVqldYDN+f01TTx3efGrMU9a93jfBG97z840sYqwbZ1BTjTALASXOqDrC1bpL+LXugDfocmrfauIb/CCLWjeyxOximi9rB9jbcyVFyuYhyGuZKJfN1fIH33Zy3vhJtd8nxZWedOjbZ3wLAd4QO123t0WUJ7Nb6+D0Vr0SpHPxVm3N07EKui5umf8N+WukVmKRx8flodw79NO29lHvyx9vPdaxRO90Wur08AqP3u0biyexeOmsdMp2YGu8MQwxW8b1IFVEJl9hzjr6IQG9p8Wy3v6D6MOV9xE+M038rnmdPcTsYY+7KyoY0YaWEW4wPHuqjB2m3uOKU4pwqRvcz7f3LUu7gHdikZqdynvaAa1CuoLwzi/PuZa72NK7m0ug822D86uJKedimfzm+5NA6tMdrdLuhfukWMrXFsc5/0Ez9tt6uR2IT6fDrGOcttHJXi7cDterMtNr7zzyqNXD/Z6DPAZz31c6vG+v/sU12i63irxrGdxGlhlTqyuivAYmX3uFO5R3IyLRn2k/fJ+eGFgHd1Z3oHj6Ts/rnWq9n0ytVmid5aZd/aPV0yOV3B61hVe0ZZ/9h3r33aW363wurRKRVGMrgoa/TeHhsRev1yY19IrvrW0lgb2h7oO3Qd6Pfj8r6BWmRDYKi95XOh67+Ax7qejkJ70iK1angZWwaFwHl0VD3LpNrF0CARR+1qyyv8CtQZeDGqVBYGt8o1rrgt+9t7iTqrgVMV43PFyVY3Soa/ifvuPC9v7Wp4D3ZxYlh1k3zSrJauw0wJZ5YmgVhGe9xe0zmvOMunu8CuqLX6k6ukEW9V3SwM0ehz5V+cc2+/M5XVolepuit+/I+kdAc3wVf7Nqv0xRKg5p7assrG5ojUF4rnkzsBW+bdXXWu95kQ3TuEU9biZ0+M/WTnPcmxRPkT0RdcPv/9x4cGf151V2L+kjql7ji9xZ1F62WHVMO99c+KvjwlT22rJKux/ed5OmV0hXJRdFdgqQs8ihlVqhNNG0UM+N/qcBh6hnmnv1cLVhP4Y74C3p5158i1vnViHVpE2xhMec+A3OMqDj5Xneu2bCypZtZB8vKK2rMLebuDRmpKPGfun+/cQwCoVHg84feM550b71ZR9V/uNejr1tPLbLR/YsXx9+RVndfOaabfcdGVVw7q0SuV5MX8x94tP6rmgm5q78l27Ro/e3OS8+vHNWrMKW+keVrxAv3/jUCHHBbcKq7zcVd0X3nOuMd45mDPdd1hX6Prl+t+kNl8GM8joHjetmjbmlJf2NapLqzD2WBu/rgrXIufZmoO8NtJoFP/c8S/myMv2C6wxzPtrzyqs+pFO6PL9XOv8/nJL6S5xEKtov2+JVMr9SjHjnhntAX4TozchJlluj3GHTuvtlT1hdg1P/ee0WctOeeXUurUKq3pn2vmnnDB03C0vKzm9NTMvG3rS0AtveGGNZ/EPDw1rp9viwOOvXyCcqL+5fVif/kMmPGPd5l0ryEY4vhM+lB+CEBcoD1pWPW/iEYW6TXqM/D/hjnDly+NO7HvCiBlWst1PYg0+b4L58pYTjJzfom6nXv+630tgf4yVD4gGYGLmSf7krGvlIU/tZI91WVHHVglHFZvXfr8rfZpTvnZdeYgPSuxct/aHMNiRBwWrvBfPguvGHXX02HWsXliFFEQinf9C7a2WrJJ5GidY5QGyCkmtMwSrXENWIal1tGCVi8kqJLXEbKvTyCoktQoFq/Qhq5CU+lUczOtAViEphbiWfLIKSakP0T2CnWQVkkoYJVtFViGp9BCyygeZZJWqrWs3VGbcBq954uLbfg4688Xt27c/OV1ajqHBVzLGKtXPD+J8fXb3iV+luTf6lZWVCYHXPNltUNBlh4pPl9S1/oCs8vdMscomJwjhH2lulcYYZeGj49nVYVplj4OZ1Cz+NXVf5HfIKjdmiFX2OIFd8F1mWeVqcEW3J2OVb+8eVGDOUf7kiAO9UcCQDo/IKpdmiFX0rILiC6ZcemxeN5ZZVvn18p5nrgzPKhOdOWYpqdFw9FtklUkZYhXOoHbQmb2fP8wwq8SltLLKX5FV3skQqxTLSUs/PzvhpCOPPOVmTk1W3TRt2jQjFfEe7S/9RYWbZgzpeczIV4VAgC+0ovfZgtFH9fvDCsZ23TXk8MEP2tdTc/80sNdxY142Zv9Im3MeW3p53yPPswP0Kp4678gjT/8rf/DjHa142gL+4Sf8r89jWGWWNs+9jM3n8+pPsC21lqqZM+6Ywwddt9JllfkXH9tnhEWDs73PXXDUEafepT+tM28a77O10Cpg30/THz+YOG2aASauvfnksmNHv2H0ip7Q5vih+tGTe61OYqPvPS4T7yzzhxEaCFf2L1ngfe6t1pGSb8rNFh19t1ne03kA8WVt8sqrjYfe5ywz6OdjjE7hMqvP3OlD69d02X3Go4ijDfj2AzMoqvGbZjyFfqP1Qs/n9ySrjDKI8U/tJzUm8afJtXatP9YMaLwVW+Ww8cbnpxuN++wQM4iKc2hOtJkQLshfkFszzaTL++rZQzws/9PfqYn9YNp9qfU8ZuFtNZliFT2eJuuc+da08IDus+ar4B7jPybzeYYbnfAIm1jmh2vreb42LcUHshY5D2nnvmvW0gI9SvShnQGc+4nxHGc+34/ttT+OZcGswvgDjAfy3zyPLDiJsR/aifHeglXs9JNz9INhQ2sy8p6PVSbZU0dWmfVcAMlahbFvbhrYuVWX0+/bWptn8OSsstp8uKr7Q8Y5o6pJn+mvvXM/T/joWG2EF5+pfXyucSL/OKLty7s/e7GNmCJkPKfZqJf5Q+ncXg+hrmBsn/4I0KiHx/F9dMBO03CQW6rnJBzMf108ouiCD+fxt20ebdLJs8wXQDwU1Cq3myHu68xkZv6wft//fsIf3WlULlpFa9u53axnOPbxB3Z+t2A+j0/qUcNmlvFTcX5ZWRn7ukw/0nUrK3vXiGpt9OAXTxab5h4a4zmgtFaS4yrvWg/gdPnMODPr/3/dfOyA5842qGRVzYxHdk41WdD54rNAejh8z5/Yu2YIRLUeVvux6Qw+avAX8xnhl/VniNawH7ubz9A/yo8D2hF4Dz8YLWM7ioznvfmCeduDWmUjd+INxnB54z1sOX9kj9+CO1kcKtJ38YR9rOZKMweTp6oeo61670HaHx8qu7V9zcyEV8xnRo1An5GPP/7D/mcV9t3vzQNzQyPdoHrBTb/vX2omkOjv536bfcwPBtv01LjIpvLy8u38Kf2NolXeMK+m2mtbfzH/4GVj9+jZcJWNjRPKLPO8xv5h3vvgfaHHtPrK+ZHgQePttsVV+v44gwW1iv6O+MONIblLjHekXsrrvF3ssfO2FHED7eNHxLaM8bSb+/hsI80jpKdVNvDFftLm4p21yC7TKvexzFTy94BWX210IHryHtabTkjRU5pv2ui750bjPDTP8zbXPOvJ1HPAeJflLvO55w4248X7mQeYu4Afud82zwIo8lHr3izi/y7Q+9ovB7eKHuy3eS/ve3yEXx0N/VC3ltld5hqG0sf+oLLKq+gbrzBWGq3Yb62iHYWNC5sljD0gPeXOr2y66mlvrzPhZQ3iQ5vzrA07yrQKMxduaJOjw4zMDNsq84zl0fsrdRfwSOQpnPxpXBHcKrv4eesJDoHwUcTDxToPd4+r6P3UX1menFLlaZW/y88p85W2YfuxVRi7i2+Kl9j3fAse+urmCssqeqb+Gu065cC9ciLfZ7GswvvGJ+iz8F96idsqDL3blr/rlHdeevKuykUsuFX0v4bfZWaPoiyyk9xW4aEPBYw1FWebqrIKSiLldz5GqZ/p3l+sMsvocPBojFytx7bHzs7g+UI8gvJPzHzTgevyTmkVvtOa8FiqqmKjxGUVTq4/Kta1S7u8zhpiH7I++8fcmgBW4QGYHbXuTmQzM942P9ZztLYZv8irPsiwRE85h22ic7zQfxL6OwTf45dF4lucRKt8++isyv3IKo9ePJ+PpG4+wvjV8PNNg2rzAli3Cj8jNTK3XA3v05xdEdQq061b7I/zP+72sAp/yq4delbeTKfoqDvkPkCvsGpsdVg1VYp7rZq//qCxmYDEj0lR12C5Hld3q3U1pnWLruAHOvFlQX923jsy387zq+BjCWP3eVrlHe0I3Hff/mOVYXwwof9hOWbWlP52v8s+/kexbZUfjcHK3vrcejpJ2z898OAtI7rsjWmVH/m+zb3xndt4R6hZuYdVvuQXX/kj//KP2ycc8W+9MvNt7NP1MR59BG8jsorQjxL2mjFWDHqgy25+3ssacusjd111gjNca7zN8+jL9GSgXO1MspZ/r9zzb3/kjolH6Y8N61dlTQfyF2LpL6jP7t//azNhv+OVf3/gpnN7SlY5LtYbfuqZVYT0/OJvtGNqjiu8yXhoUr//w/b09kjcVVpFjPyNvM48rCK+t+omoza9q5HNzyRsJ5h97ZhWMaJtmhnHu9ecWKxByCr5KE5deFXTlXx6vbFYMf+7zO7G7uiCwxSFlR6sCriqn1bZ4MTnHL3aOuZrOqS3fSrX92+eGVCy3ckdfSy2Vdg/rfedtJzDPK1SMz1XesruKf63+a47fjFTvCeAVdhR/LPxVr/Lfldnsb0s3/kvmGm61xv9n3tt6xh9b8O2jfUuSq49Ivu985ae17FV+CBQ5Ov956iy+cHfdW9W0OLwP7xr9h9n/7ZZo1637np06NChT+kfVJ6t/XmDvcCHl5QW5zc79PcP2W9hXKaVD+Wd3fu1f6/V+5CajJcFl9/Rv6So5aC/G5ErH/OCNdYiy/TPNt5wTMv8xp0G32RGbP5TiDpd1a+wp3Ar8/yhjuYaq7Nuy77CP7OPPzv/NqRtYaPWx012uiyTtPK9K8e0K2o30rpyY1tu6d+qoPFBA28w72E/f2yTBgcNN/rTZ7coatl/vdFpHtuteUHzsuGPlTO00p+HNejw7H54BZQu+q5lPMQsab+1yr7H+Qv8Ip/TXiWr+OvHZn5Z4iSyiiOdXzi9inYqWSWWtOuLrCmVtE/JKjH1RMuxX9IeJauQyCoksgqJrEIiq5BIZBUSWYVEViGRVUhkFRJZhZQy7Vsx55OdqxbsJqsE1pYpQ557cehV5rMfO69sl9d+qpmn9tGfWqtbeR2AVwViCf6+dmSBplVTDh84oPuff/RYj/dKV0aNB8t4bEL0pTgMYa31EvZLly6tnKeh/8EfhSzsYr62+dvRrfPajOWgH3+a5BnjMSq4hayC1B0W8seVS/WJaiNsZgDPsvh3e2vHemlBtlWGKkAlnlZpwiemjvmBR3v0YvJ61CvlASmMLXRKtywvj8sqmv4IcKjx+Qxo/eCHc840afG5Rq5Ew/cMq7TbQ1bxUkNYzyN7jBe7Pg1Fb+75V4H+LPtHjS/5pEjVyh0dToqCRwWoBH/fbP7/KTZ0zdhqaMDk9ahXCtFDJjB2eRej8n13dQSIDPwtwLl6xkq06q9lhYVVlqvwazWyzQorm8Nw852DB/C4DranCDZp/2xuCoOWViwZCM23GJ6cLixEVrF1Fn8M559GzA07VUfZJgN/zWuNdhpXWuX86GrLEKgCVII0fCTfwyXgUJWzDBgXrUe9UojeUrx3b/FteuV7BmjHgPb8sY7SK9nDvXtDHo/agH3+VnkRjlkNw/Q/m4OeYnQ0f7Be828pX3JvN/gzXzqS13ArWcVD69t0XvF1l7bGE15t9MiS+fbLjFVWeRZuZJYhUAWoxK3XnQfXd8ztHLFzcdB6VFZZm/X6a5H1euU3QNNXGdt6jnU24ik+zyydV+NvlUHwCDsqW484uwyKRtzzygdGKnc3ngSk6Tnooa/oKhhHVvHQx/r7kKKv6hNRfjJh39pvMBH22ltlmvoaf69r3LnCNgSqAJWgZXSdztNYnL5Lnx3xWIUdN+ysAUblHeA5/TTU3bbKtTVit8a0ytP/FK2yPlKwg91nvHTwVyPGKe9P/HBSAMZjQV9Dob6ina2yl5NV3NI6ss16N4eIHhmfA/z5wPVeVtHDBYr0P6v6wX+ZbQixAlwiLmP0RHMK7HT93xzRyHmePZhVHolGnzIqzzNznceAXSjo5ZffNT9uLVrlZigoKzsYDjAeZpx/9TlHNQS4Wvsz34w6XMejFnhdz2qnRrKKS4UwoZJVToR2fKKF/mTepzFOQI9DtH///lnQ/2y5Alzi0h0wQpiqmGpcDgW2ys/5RbsMqxyip3exqkM9reIsY3yPSK7eDTrIvBj6P8bW9tO7SVV38UwQ7QLcePD6Lehu1nUszCaruNRAfxh1g3E1Mhju0q8kT/Xda3daV6Ct5QpwiUtd8TvPN8dpFfbsC+Yh615o9jpjP5wPnlZ5/vm3RavsMH4Gc6GLfr7kj/z/kpWjx94ap9rJMFCf/USYYta1OLsHWcWl02BiJds72Xj2+P+g3Ua2oQ08G6Nba/RrPCpAJbI+gM5mj+KJy1cxtnu8fdkU0Cp25dWjARp1iIC3Vey+CmT/rYpHOJ2uX5qBEbDQmV/1dIRTt2kmGqlnp25sAldrB8aroOlmq67LfIaU9lurfN0cmvZsBs31iJLq46GgZz6cyIfg7te6pBHQ/uf1nvIny8qyoOwkVwWoRNYYOwm1L0D7QxtAq7XyelQrXVsGWWVla7/RK5+tffDa4OIGPa4ZFtH+vKlMLyw72WWVsQCNe3UA3n/aWpYHncoYe6SsKbScxNpqHfEupQWQr2c4/7cImvZqAg3mMjZJr+ub7cVkFbfWjW6Z0/JCM8N615S2ue2u0p9qn2qdTK5VnoNauytAJVi7GhihGvycML1nYU778Vtc61Gt1Bg+XbEMzJBbU8MKDUPoau+yCvt0zEF5RUc+y4zMSb73b+H/DN0biV4UBcg6dqE5Gnhhq9zWo79hVsDtMvYwWaWe6Jd+r1VqnZ0Duie4/LbFbM9KvzsD1Ys+JavUC80ByO/aJZfnN9d/kVWS0ryLDsrN63Y/I6uQSGQVElmFRFYhkVVIZBUSWSUNhZBqfLcQQd2O9BHTGca4Lh/qdyBoIyg9q7Db5C3WhMNuCxKQ6nLzdST6OO+5PsuQVepcIlKNrSJC3T5WESBoZ0+32sT8d7uFVJNVMkwmUo1v8YpQt/Tljdn0G/0YgtaKqveVv9cLRuoTOrvNPB73EJDqB01etiss8F+GrJIOMpFqbBUR6lZbBUPQZtEiaG7u9ofu9NztAlK9o1DPh19oPCjkswxZpe7lINUQKS2Idplq3IeToW5vq2AI2iz6Ghqau53v8hHunG0BqWajgQ/wTzDfTahehqySBt/FRqoBIoccnAMd9Jh7GerGC1j9CQxBG1b5dTgMtefzWqOIVLMPoad2VDogfzvzXYaskgZykOrHbtY6HN/1BZ2tlaFu4ctnlXLpuxRD0FpR796HFkCTr43dXprjuaEwUt0DPmevwwWWDRXLkFXSQgJSrelz4/QhQ93eJyAMQfODQiS/08XfiCeTmVOltzeLSLWm+2ECO9vo1KqXIaukiTaLVlliPJYqQ93eVsEQNO4Ya7t94QL+Xqq1eHkRqda0vaD5lmi3GMuQVepcCKke9mIlY9sGGS+RkqFub6tgCNplFS7XbkdItaYL4CT7hduqZcgqdS4Rqf4coGGvblFopQ+9ilC3qEc46vw4e7IMQPtHhKDZBIOoNp5Tn8Bn4CrCu11CqvWrLDA7taplyCrpcKUsINVszohO0byDr/jBKBKgblHywL4DQVuDrcZz6sLIK9rtGKnWP+pmPYymWoasQtoPRFYhkVVIZBUSWYVEViGRVUhkFRIpjaySDknQFd+uo7ftpr1VxCRoMSPawFStEBRQUap4NrECfah0uJ4AB2/4Yq7LzogCNLu+9RmMjbdmmiyttDlPI+TJPK4Mjmn6DNvMZoB6PT5J3coIbt/cqf3MKjgJWsiITsgqYgWBrfJGQf7EWa/9McJDk1JpFWVSt08EN1nFEUqCRkAzuq/LN5Y+6UGpotu/YgW8oGoiHDDPmFIQ0WuK8vU7whNhgPGBHQmGVrowu2gdY1/l5S53N6DIWc6nocqkbp8IbkUE4X5pFZQEjYDmxKwiVqAV/DQAjlzPLKt4EtGXwXX6v1/pAY9Kq7DJMISx/nADS9QqyqRunwhusoq4o4QkaAw0J2QVsQKILu8IYyqYbRVPIrqjee6ruWOOr1V2d4KXHodulQlbxTepW5FViEjy/dwqKAkaA82iB4aNMyfHDfO1CqoAIg2F1HIVEZ0H0o6wd6C00rlQUpz1PvO3ik9DfZK61VYRSfL9/mJZSIKWgGbTAyeV+R+MxdlQBQCNOsHLzlb3JqL1LoSnVeSVXgRwGYthFZ+G+iR1K62CSPL92yo4CVoCms1N3hoCWYXPhiuAyKcfR4q34hOQi4g+QgiJ9LdKOUR+8WpAdhYflKmEXH+r+CZ1+4blmiT5/m0VnAQtAc3xWwVXwAv+BGc6VvEkomdAmdH/eHJdDKtYF1GySmAF45fBrf2t4pvU7WuVJfZl0/58AkJJ0BLQHL9VcAW84NeO8Azazy6r7OgAJ2tdzVVDoWdVYlb5PVxWw6ouhNH+VvFN6lZYBZHk+7lVhCRoDDT/YsZKl5Xl8tZtNSYfdx2WhNlwBTpEfY12oCk89LD5fkT08rYQ6XpQFrT5j3aIKdMjrsvK7mHySmeW6VU84v4OqxpBSe9iaLaG+TTUJ6lbHcGNSfL92ypiEjQGmsvxuOcGeVQWj9bqwhUYo7V6ZxSP1spWYduvLy1o1OdeHr2CRmvxSo1RWc93CH5zYeu8VqONV72oGsrUSd0+EdyYJN+/jyoxk6BJNARHIquQyCokElmFRFYhpY9Vvp85h22cObcOG1/3Lait5qTDN03CKrPhAvYv/troOlPdt6C2mpMO3zSmVdSs9Aswnj1tBNfUkeq+BbXVnISrrtkdf0mCVhFZaYkenQnXsAdhusdCYvy0OqUaw8krz25a0OcVryaoUqqDtgBPoPUoG8p1nRfkgrbBkIAbJP6qh+n/RBr2e4HF800R1M3Y/OHtsuGiGCVhWQWx0pJVZsDt7DbweFEpip9Wp1QjOHlFk5KuJQAe7+tSplQHbQGaQOtRN5QJrHRgqyibE3/Vw+w/n2OBvynmuNm+sdCs5/GjFviWhGcVxEojJHCA/WVmyQu546c9U6oRnHwaX8MzWXmuu2PqlOqgLUATaD3qhoqsNN5eGIsUCG+f5iRSNWTXVO1eNVJ/CCHgN8UcNxsPM60/1SXhWQWx0kGt4o6f9kypRnByA36IYefBjXJl6pTqoC1AE2g96oaKrHQMq1iEdyyrxFm1YcJ1Os0T8Jtijnt55BjUL/EuCc8qiJVG32Vo4zwo1P4ravymvJAcP61IqUZwcpF+41ZEOkypU6qDtgBNoPWoGyqz0j5WsQhvn+YkUrVhlUf1aNQ4trVjiOth1OktigYvYTFLwrEKYqUl0PgizePnwTz3QnL8tCKlGsHJg6HkO/ZCLvSTK/NLqQ7WAjSB1qNuqMxK25K2ASK8lc1JpGpOyPQogeibLK5t7RiC77niAmiwNGZJWBfLIiuNTyFnaienweCxPjl+WpFSLcLJ7PMCiDbVvoIrXNYvpTpYC9AEWo+yoS5WGm8z4dETkfBWNieRqo0TzmlL49zWjiGOh5yn2J6z4JSYJaFYBbPS6Lv4nD/d8dOeKdUinMzYZycVtvjdONuSttQp1UFbgJsjrkfZUBcrrbSKQ3jH6qvEWbVW865OZkcujm0tHjsm6QfjBjFLQrEKZqXRd7mkrBC6l+VBj7J35aU84qe9UqpFONlQ1WHwuVyZOqU6aAvczbHWo2yoi5X2sYpFePs0J5GqeV9lQQQejXNbO4a4Cc7XjzcNY5aEcwJCrLS0mXrAVs3UFe6FUPy0OqXaBScvGgxjXJX5pVQHa4ErDdtej7qhZj8g9glIJLyVzUmkar3mKVC0Ko5vigyxJlc7tVddYm9edUk4VhFYaSaDxm1hD4sWeiyE4qfVKdUITp484MQSgPM8HvZUplQHbQGaQOtRNpSJrDSWuA0kwlvZnPirvpLXfDKr6AEHll0T9JsijpuxewA6tIDGX/mWhGYVkZVmMmjcMJftgTZei4nx0z4p1SKcfAlAydlzvMeuFCnVQVuAJvB6lA0VWWkscRtIhLeyOfFXrY/Wttd+XLk6UB7sm2KOm7GXjsxvfOYK/5Lwjio+rHR1VjPt9HtYSPfDqnbsi3uZRFqQyHpS2Jy6rzrEi2X1EefOh1n5nc/XYdvrvgW11Zy0+KZEwZHIKiSyComsQiKrkMgqJLJKGikdQrMzVjW7a8kqJg3caaR+d22IkitVxwgPUbLFYn41JyieYSuMbAqcUIxAcD+pyWt1yLSSyU5oQibJPWFrv3DuBPSHI467+5qhn3kXOrC1K4/blDq/OTGr8P37XGhWcdhiJ7+aW6XdHk+ryKHZKinJa5+QaTWTnciETJJ7w9YhWwVgNWNPROd7HY0F2FrO4w7fKgYNXLHmEjjEnNL/cSHIrkgusQahRGSLpfxq0B9dyLYrs8KspdBspZTktU/ItJrJTmRCIskVsLXfpkpAr75YzVOJDvcoEmFrKY/bkU/Ud/xW4f/fBHnWlJMrjSe07//gDJVVrBKRLZbyqyN5DbcKVrHDrKXQbKWU5LVPyLSayU5kQiLJFbC136ZKVIucO6iOEGwt5XELUkd9J2KV6q1XmrZFudJ4IqqbxpU4jUtEtljOr76KP2dpWcUJs5ZCs5VSk9dMGTKtZrITmcAkuQq29ttUCWp9d2jq/hTB1jiPW5Q66jtuqxjK+p8zhYrwfOoarL8FthjnV0d3tspebllFCLPGodlqqclrpgyZVjPZiUyg2pSwtd+mSkzzDwSvR1QRbI2Qdyxl1Hf8Vikt7d4hFw7dZE7lCFtdnMgq9Y6cRiWILcb51VHtdzjIsgoKs5ZAcIXU5DVThkyrmexEJsTa/GBr9aZKSDNztS26x/05gq0R8i5JFfWdWF9l9xX8jCblSuOJqKqjJpaIbLGUX63NdSzMto4qdpi1GwRXyIe8VodMK5nsRCbE2vxga/WmSmTc5HKA7FtrmNdRRYCtMfKOpYr6TswqbK2RJo9ypfGE9v0XLlBZxSoR2WJ3fvXi7B7OFZAZZu0GwRXyJa/9QqY9mexEJsTa/GBr9aZKQGO1w8W77PaJ1vTzz79t/oVgazfy7t7H4Vil6gbz2T6ROsYTytfXiCUiW+zOr9a6sGBbxQqzdoHgCvmS1z4h095MdiITqDaz/xRrgySr2wHarWCsVOgVWJ08BFu787jDtsqVBmfcrQFkvyFRx/JEVlmZV7ivWCKyxRNRfvUkfa5vthdzq+AwawyC+wzBqchrn5BpNZOdyIREkitga/WmSkBVEYBOffr0KfCwCoKtpTxusaujjPpOaLQ2t+XwhUyijj0mxjPV4OR4p7b2Blt8JsqvNq5ylrGH7aFfK8xaAsF9pCKvfUKm1Ux2IhMSSa6ArdWbKpHbY66RX8EqCLbGedyCfKK+0/p2YfWiT+WPUhmaHS6TnULCOy1EEAKJrEIiq5DIKiSyComsQiKrkEhkFZXqJe5daxj2/iQB9zZGMLcZERYYRZdinoWkblQiMubl6ncaJq8kMWyySgIScW8Pq1goOraKmNQtmchhzFNqlSQxbLJKAsK4t8FcmLeGRRQdhcuipG4p0VZkzEO9mYyVLIZNVolfGPeWrML/b6DoyBAoqVuyisiYp9AqupLBsMkqcQvj3i6rWCg6MgRK6pasIjLmKbZKUhg2WSVuYdwbW0VA0VFkNUrqxmHWiDFPrVWSxLDJKglscgH3lqwioujCjpeSukVLIMbc+vykshQ0O2kMm6wSnyTcOzuLB55WQq7TV7FQdMEQUlK3UIIZ83AfKMTjJslj2GSV+CTh3iXA6bHFBsSGUXTBEFJSt1CCGfMUWiUMDJusEp8w7v17uKyGVV0Iox2rWCi6YAgpqVsowYx56qwSCoZNVolPGPde1QhKehdDszUyio4TwcWkbqFkK2LMfzE+15Qb9tYOAcMmq8QrGff+5sLWea1Gf8dkFF1KBBeSuoWSDYgxL4dwczIEhYBhk1XiVipx7/ohsgqJrEIiq5DIKiSyComsQiKrkEjJWaV86cp9nhMxGGaHBq74dl0VbfZ6aBUcyLtkYASgye2dj9srT/hGVjs0MFt2RhSg2fWtz3CzqMlKmVKNVoN4ZFcD7PxqTDeLcdqoBFfgG43tvUWTl9i2gBtEUIgRx+iLvV3Y7J7P3rkoAl2ZNOEXWS3SwG8U5E+c9dofI/yeXMhWUadUW2vR3/SMeGRUwsT8apFuluO0Be4ZVeAbjZ0iq+C2BdwgqbGKmHn9fbMG+q2n6VDG8IRvZLVAA68pytdvtE6EAS7AMFmpU6qNbzDFiL9BPDIqQfnVIt2M47QR94wq8IvGRls0xNvMuG0BN4ioENOwhczrqTDN2KLZ+dV4wi+yWqSBL4Pr9H+/4vxQyFZRp1QPH8mPbSVGOCDikVGJO7/apJtxnLZYgivwi8aWrRJWGrZH22JvEKTw0rCFzOvuYJ6hX36/Gk/4RVaLNHBH8wRVc8ec0K3im4bN2OvmccDNI1slrvxqkW7GuwNzz1YFPtHYLquEmYatsEqMDWIpvDRsIfO6AH5wPkYTfpHVIg2cB8KdW4wtJy3fNGzGTocH9X/dPLJVIudXI7oZ7Q6Je7YqUEdje2zREAcpFFaJsUFshZaGLWReF8J3jP1SynU4nmA+kdUiDax3FDxO22HINw2bbckpMA55Lh7ZKpHzqzHdLO4OiXu2q1ZGY8tbNNw0bJVV/DeIoLDSsIXM60PhReupyihDE36R1SINfAQ8nzKr+KZhsztgBDP9LvHIVgnOr5bpZmd3uLhnu2plNLb3CSjVVvHfIOJYWUhp2ELm9XXQu9I8tPERCGHCL7JapIFnQJmx0JPrQreKf0p1V+uZUBePbJXg/GqZbnZ2h1ziVK2MxvawSmhp2LJVHAw7Rmy3+yI3rItlzSo/tYKB/BGpDRwRRRN+kdUiDbyjA5ysdShXDYWeVWFbxTel+gPobB4HZB7ZKbGNzzzoZnt3uEqcCmJEY6fiUOq2igNM+sd2h28VlHm9uC1AhyPbg141mvCLrBZp4OVtIdL1oCxo8x8mAc3JD8EpU6o1jbFzCGQeeYyQUGDnV2O6WYzTdnPPTgV+0dhoi4aXho2jvrFVfDeI0PMKKQ0bZ16zn2f0aZRdfNzd+qMMwoR/ZLVIA2+/vrSgUZ979QHmDeGGSagjq9muBtmbrdkwjyyWOPnVmG4W47Rd3LNYgU80trxFQ0rDlqK+kVX8Noig2k7DJoZZVj2MxiYIgURWIZFVSGQVElmFRFYhkVVIJLKKSpSGTVYJpsxMw1415fCBA7r/+UfPQkrDTokyNA176pgfGHsSenkdJSkNOzXK0DRsXas9M94oDTtFyuQ07FngwelSGnaqlLlp2Dvmdo584f6Y0rBTpcxNw+Zt67PD9TGlYadOGZqGzX5zRCOAsa6PKQ07RcrUNGxdFVPtZ2nFowqlYadEmZqGbWizbRVKw069MjMNmz1x+SrtgDfevrahNOzUKyPTsBnrC9D+0AbQaq3LKpSGnSJlZhq2dhic3rMwp/34LcxtFUrDTpGIJCerkMgqJLIKiaxCIquQyCokElmFlN5WqVXUOWkEuWY3GSSoVTCCLLLFnIZ4hq0wAxeClcQIzU5SUt6zEMHNhJhrjC0rY65xBXKUtF0bLkHB1KqU6iEpHLqte6tYCDIT2GJuiHZ7VjjZHAFKfEOzkxXOexYRZCbGXIvYsk/MNapAjpJ2ahNLUG3qlOr6ahWMIGO2WPuu07V/siF4iV9odrLCec8igoxirnWZ2LJPzDWqQIqSFmoTS1BtPinVKScS6s4q/P8GgozZYojkNdxqGyJQiU9odtJCec8IQXbHXJvYsk/MNapAipIWahNLUG0+KdWhpmGnm1UsBBmzxRC9CsbZhghU4hOanbRQ3jNCkOWYa4QtK2KuUQU4SlqsTQ6ZRrX5WSXMNOy0sYqAIGO2GKI7W2UvNw0RrMQnNDtpobxnhCDLMdcIW1bEXKMKUNWoNjlkOphVMrGjEsgqAoKM2GLtx/GsdiA3DBGwRB2anbRQ3rOIIMsx1xhbVsRcI4ZZrBrXJodMB7NKyGnY6dVXMRBkzBbz4+ixMFs3RLASv9DspIXynkUEGcdc6xKwZUXMNWKYxapxbXLIdDwnoHpqFQNBxmwx/8aLs3vohghW4heanbRQ3rOIIOOYa0ObPayCYq4RwyxWjWuTQ6YDWyXMNOz0soqBIGO2WL/mu8w46wYs8QnNTloo7xkhyGavwvhXxpYVMdeoAneUtFWbXBLYKvVvtFZEkDFbPElHi7/ZXqwZImiJb2h20kNwKO9ZRJCFmGuMLfvFXIsVSFHSQm1iCQqmdqVU2wo3DTvtRmt1BBmzxca1zDL2sPZR0BL/0OxkhfOeRQTZibnG2LJvzLVYAa5aqE0sQcHUrpRqabR2fL2zSoAf86JPg5cQ6pzJIgiBRFYhkVVIZBUSWYVEVqnnWvfRJtoIZJUAquqctYS2QhpYpcgZ8OJDfQvSb6VPwZn1amcnh5UnYRUVaMzQwKdvDnO2QLYGssqO245slNduxKKE2xbPSqu7mu++lTFs+Zs6NyKvq3UQRdk2tvLspgV9nPA3zKUz5+cCEGnY74UUWkUNGttW0e/e+uYwx2uV5e2hw/CLh7WP3JVo2+JZ6fPm7UYZw1ZbxQG0a0vqtq1oUtK1BOB+Y0ri0rFVuJ5LnVV8QGPjbvQU40aHbw6zuNcePbdJ/hFP+a7z53aR+1aekH9s85vNvL742xbHSmt6gHFrQsKwxW+KbxLLuHctSN220zjs/kxWnv4CAIlLRzurpmr3qpHWQxWpsIoPaDx8JLdxiXH09s1hFvcawAEHAUz0W+dNMOWnNtqMLdkhJyTYtjhW+rLBwLkwbLVVZNy7FqRuWwN+OGfnwY364Rhz6a7f9boACFFS36zIZ+nXTXLbN4dZ3GuRhxl7twn802d9vWDzn6D7xXABO7pR4m0LuNKaMvjY+EuGrYWtFyktiHaZat4AlXDvWpG6bUX6U1hmUiDm0t1WedSKU60Lq5wOD+r/+uYwi3ttDP/nATjeb30lrH2LrVvhyZoWTcOxis9KX4OTzL9k2Frsq0QOOTgHOqzXf5kS7l0rUrdtMJR8x17IhX58AmHl0lcoK+tRAtE368wqW3IKjGc4fHOYxb2m/7LXGIiiQvltWd5UNgkWvgIDwrGKz0p7w/vmXzJs7eixm7cw9l1f4KCtC/euFanb9nkBRJtqDtFPTQgr9+iZn7aU1ZlV7oARxh++OcziXpvF9J9Hgc/6Ds7Z1a3s9wDNsyMfhWMV9UrfdNwow9aunaJbzQP3rgX5tO2zkwpb/G6c8XQEwsrlE9CuToFe6RGeVZwcZq6u1lOpvjnM4l672DgX9PRZ3+Uwc/aBXZ87p+Tof7NwrKJeaR/4n3Mox7A1/qaMLdGv77xw79QrRtuqDjMuLhBW7uqrLIjAo7VpFTExlX0AnWusazafHGbUw3yEsblN4F6f9a1r2OA/eld54pE1IXVrVSudLfRfZNja+abDXqxkbNsgOEvoO9TuCUjdNq5Fg43emAeXjrq1U6BoVeqs4gKNUSPH2O9fU+Yw36otq0Pef2HsT6X851jcDuDYvX4rndMIeo8Z1ScCQ36Jq23xr/QYeNcZ5sIYtvNNPwdo2KtbFFoZYxcioF1rQ3CqtrHJA04sATiv0pjCXLotnbM/mVX0gAPLrkmVVVygsWiVXQ2yN9uHAkUO8yVWBRONQcMpHQHyLtrlv9b1k7oUFHQZ92GcbYt7pXONCwfr6yAMW/imc0Z0iuYdfMUP5uSdtX8CUrdN+6olZ8+x50NcOh6tba+ZPhdgeCpPQCHr26W/pMtK+8PbLMNVtSPkJ8MJQvDSfOhLG4GsEkSbv9hCG4GsQiKrkMgqJLIKiaxCIqvUZ9VqUndtKcRAb7KKJSGpe5o+6rrNeBUhTgSX3kP47ejWeW3GrnGViCHg5ehthymQkv1WJIKTVZKTmNTtYRUrERxbZW4D/a+G77lN5ISAp9oqKvZbmQhOVklOOKnbfPu2cZ9YTATnd3h0opY/PLK5KQxaWrFkIDTfIpXgePCUZnop2W//RHCySsLCSd2SVfj/jURwZIgpUMrvs+ztBn92WUWMB0+pVVTsd4xEcLJKwsJJ3S6rWIngyBDd4Hl9tud4qKJkFTEePJVWUbLfvongZJUkhJO6sVWERPBh48wdP047YhTA1/psX0OhVILjwVNoFTX77ZsITlZJSmJSt2QVIRFc3PH5YGA46yw417EEDgE3Pz+pLOw2+7DfvongZJXEJSV1Z2dV8a4p5Dp9FSMRHBmiq/mQ41vWUzR2CY4HT917XXzY7xiJ4GSVRCUldZcAB8YWG0SbmAiODDEZBur/nmi+xMEpwfHgqbOKi/12MOxYieBklUSFk7p/D5fVsKoLYbRjFSMRHBliYxO4upJVXgVNN0slOB481W+LQq8lsYBJdSI4WSU54aTuVY2gpHcxNFuDE8G1kq1lepz14/pC/y2Cpr2aQIO5DJXgEPBfjM815aZia2P2WyCclYngZJWkJCd1f3Nh67xWo79jOBGcMTPg2xp7XX1hq9zWo79huASHgJdDSl9XiNlvEYZXJYKTVZITJXWTVUhkFRJZhURWIZFVSGQVEomsQgrdKuVLV+6r7VbVSyK6vltlycAIQJPbOx+31xh0nGckUpyLZrrFGKXUBwRnmNxHyeC35LoQ0Iz4Vb7MM2yFPnELIqKDC8VCixPqcG7XIKpNKv/hiOPuvmboZ15ViyHTqAK0jDIEfEjIQ7fKNGzUAlVzmvNIRyNOvnWSVnm7sNk9n71zUQS6sviswnWXp1VMoNlllXZ7LKuIRHRgoVhoNKEO50YlTCSVAVYz9kR0vrtqFDKNKkDLKEPAQ7aKOg0btUDVnPCs8n2zBvrdpunAoZwHTVa0qztv3Lx1mm0ChtVVO5dfChFXbKEINLtIs+lWBZiIDigUC40m1OHcqASRyq++WM1R+8PdVaOQaVQBWsYnBDzU28zqNGzUAmVzFmYXrWPsq7zc5UmegKbCNGMjZudrm2FHYd6PvHboxmJYxfz9jHNbhf/fAJolq0TyGm41K8BEdEChWGg0oQ7nRiVuUnkRwD5XbShkWqpAWMYnBJxvqwdnhGQVdRo2aoG6OZM5sNUfbki2r9IdzFPvy+/zI9xo/bg7Ae4LZJXZ0NnLKhbQLFklehV3ll4BJqIDCsVCuzOi1eHcVomLVF7fHZq6q0Yh07gCcRmfEHC+rTSfjBgUhlXUadioBerm7O4ELz0O3SqTtUoB/CBOfshTO/cekL/dXY14/jWtssnADT1m04Fm2So7W2UvN6yCieiAQrHQ7oxodTi3VSKTyvMPBBNuQ7WhkGlUAVrGJwQ8VBxBnYaNWuDTnLlQUpz1ftJXQIXwHWO/lHLpv5we2qH2dbjAo5osfSZklQ0WYChuJQFolqyi/aoHWUGhIhEdUCgW2pURrQ7ntkpkUnlmLsBpe9xVo5BpVDVaxicEnG+rnLCsok7DRi3wyyS/COCy5C+WD4UXrQcp9TPM/TCBne31Eh2vE9B7Bl/q7quYQDNGnbUKjoXZvAKJiA4oFAvtyohWh3NbJZhUrrkcIPvWGo+qUcg0qhot4xMCbp6AwpE6DRu1wC+TvBwivyRvleugd6Xw02fbC5pviXZjwaxyPlzqbRUTaMaos1bB4uwevAKJiA4oFAstZ0T7hHNbJZhUHqv9Ct9lt09knrXZIdOoarSMTwg4/6oLw3r9njoNG7XAN5Pc3CvJWeWnVjCQPxW1waJCL4CTPDq1XlapuDuS+6V3o0ygGaPOUb0/q5/BMBEdUCgWWs6IVodzOyXiT+J2gHaaj0uBedXmhEyLFeBlfELAQ31+TJ2GjVrgm0keilXY4rYAHY5sD1Zt8wE8OrWPcM74cfYkB5Yfn6Rjy13zIecJaTYJaBZQ50k6qPzN9mLdKpiIDjoWJcZCyxnR6nDuMUJCgU0qV0UAOvXp06cA3FXjkGmnAmkZZQj4BP2rhhaLoE7DRi1QNofNNOLBH0l+YP/nGX0aZRcfd7cVbt3NOjPHGK0t6Dx2iffAvg00C6izscwy9jC3ikxEBxSKhcYZ0epwbrHEIZX3SQOqYm1SyLRdgbyMKgTcGK0dH9phRZmGjVugao45aq6PACRpldpV9SL+tsC0JqJDD5nOGBGEQCKrkMgqJLIKiaxCIquQyCokEllFJWK/ySrBlJlp2Oo0JlSCsXKySlLK0DTsgFZBiDhZJTllaBq2umZUghBxskpyytA07IBW0WUh4mSV5JShadgQKS2IdplaHqDERsTJKskpQ9OwtS7UIQfnQIf1MUscRJyskqwyMQ2bPXaz1k36ri+cHatEQMTJKkkpQ9OwTX2uBxn7lCBEnKySlDI0DdvUEvsRUwfDRiUIESerJKcMTcMe9mIlY9sGwVl2D6WPRwlGxMkqySkz07A/B2jYq1sUWq2RrSKWSIg4WSUpZWoa9pwRnaJ5B1/xA3MdVYSSfaGsn6xiitKwySoksgqJrEIiq5DIKiSyColEViGFb5WKb9dV0YYixbTKsjOiAM2ub32GxIiOtwYAJzOf3F7MMItwslnSsN8LIX0TZQA2QpDVE2psedWUwwcO6P7nH42pj/7k3MpBJS7U+Tqv0dEQIo7FFuAJUYHCuUNMw2ZvFORPnPXaHyNwbBJWsRhmJsDJdgk8F4pT1AHYCEH2m1Bhy1PH/MDYk9CL//3v9uKXFEtcqLOTrR2qVVALcHPwng0Szh2iVdYU5eu3RifCAH1uTHOJ7/dV3TtFAdgITuYlu1eNNK2TrNQB2AhBVk/EwJZXGzlqHzW+5JMi/CXNEnkZIVtb2iLJ3WZGLfBojudXSH0aNrsMrtP//cogfvyt4hnxjAKwEZxslKyLKxxQLXUAti6EIHtOxMCWZ4HOPtXs5r9BNJtZIi8jZ2vH3lTBhFrg0RzPr5D6NGzW0XwlQs0dc2JbxTPiGQVgIzjZsMqjFkGWpNQB2FwIQfae8MWWd8ztHPnCmkD7BpUIy8jZ2iz2pgou1AIfqwQI5w4rDZvlQbnHsdPLKqqzLwrARnAyx0B6lED0zVCsog7AZhKCrJjwxZb5V+izw2vfoBJnGTlb27VFkvqyAa0SKJw7pDRs4xznsoqFE4tWUUU8I4YZwcnGJjttaTgXQOoAbAlBVk34Ysu/OaIRwFivfSOWOMvI2dpoiySfhh3MKsHCuUNKw2ZHmE+6ePfI3CcgjxUIDDOGk7WSXZ08322RiNQB2AhBVk8YUmLLFVPt1wZJ+8YqEZfB2dqeJ6BUWyVoOHdIadhsBpQZZ7En18W2imfEs8gwYziZlyyIwKPhWEUdgI0QZPWEITW2vFllFatEXAZna7utkmQattoqDoYdNJw7rIjjHR3gZK0vtGoo9KyKaRWmbofBMGM4WS+ZAkWrQrGKMgAbIcjqCR9s+YnLtSbuHm8fAJ19I5Z4oM6+F8upsYoNTAYO5w7LKmx5W4h0PSgL2vyHYZx4RpmeHV1Wdg/ziXgWGWYMJ+slJ7OKHnBg2TVhDMEpArARgqye8MOW+wK0P7QBtOI/zPu1bxPhX+kBqcSFOtvZ2lhJp2GjFuDmCFYJGs4dYho22359aUGjPvf+aoc/mP13j9Faj4hnkWHGcLJe0l7bRVrfa3gYhxVFADZCkNUTftjyjuk9C3Paj9+ij89aJddKJS7U+U7vE1DSadioBbg5glWChnNnbBo2KY1FViGRVUhkFRJZhURWIZFVSGQVEomsolIGp2HX7Car1KIyJw1bIq/nD2+XDRe55sLktQipk1WSU8akYUvk9b6x0Kzn8aPcd6oReY0gdbJKcsqYNGyJvB4PM73nQ+Q1gtTJKskpY9KwMXm9PHKMYj5EXiNInaySnDIqDduxyvUw6vQWRYOXuOdB5LUbUierJKyMSsN2rMJbXVwADdyEMiKv3ZA6WSUJZVAatmOV4yHnKbbnLDjFNQ8ir12QOlklYWVWGrZ4VJmkH9bc0Cwir12QOlklYWVWGrZjlZvgfP1wYQWnOxg2Iq9dkDpZJXFlVBq2Y5U1udoZpuoSm692cmsReS1D6mSVJJQxadgSeX0PQIcW0Pgrl1UQeS1D6mSVxJU5adgyef3SkfmNz1zBXFbB5DWG1MkqyYjSsMkqJLIKiaxCIquQyCoksgqJRFYhkVVIdWEVhCBjRlQEjR2wlI8gBozGHqLCljmC8QxbYWY5oHAOP4mgsSv82QmmFmdDYdZoAlcgcs+oBE0ES8MOWa6VOlp5dtOCPq/g/SM36AmduN2jF+0LwSomguzCiW3Q2GnKoyxwNPYQFbbMrdJuT7xWQaCxHP7sBFOj2VCYNZoQK8DcM6oaTQRLww77py6t1NGKJiVdSwDuR/unScqsIodZI5oLJVvzu/X/ggE15kL6P7GisdFsqDaA6do/2dby0QCbHIHGUvizEEzt5pFXi6yHNSFWgLlnVDWaCJqGHarcsd2WTuNA+TNZeWucTT3FIwLoSui+iy3IiS5Muq+Cw6yRVVCytWaVTcXNNtoLPXQnixmNjWZDtUXyGm6N0yoINJbCn4VgajePjMKsrQmxAsw9o6rdIdMB0rDD1yKvQ0IDfsRj58GNfMIdD26pagD8/vuW8DgLxSpOmDWyiggat+jFqk+El52F+P6PFY2NZkM52dGrYFycVkGgMQ5/FoOpZR4ZhVk7E3J6tGMVVOIKmQ6Shh26ULo3c5p8i/6jcJICX/c6+LBt7aE1XB7CFRAKs0ZWwaAxY7fBhWihALUJs+Gc7OjOVtnL47MKAo0RgoyCqWUeGYVZOxNyerRjFVQizxYoDTtsodhuR4Oh5Dv2Qi70sz8R4sFFfZYNffaGYhURQRZxYgwas09yOv4iLJQDAWoTZsM52VHtRzkoPqsg0FhEkHEwtcwjo5hrZ0JOj3asgkqk2YKlYYcsHNvt6PMCiDbVjGyfaoV4cKQHAZqvZeGcgBwEWQD/JND4l47ZH9q9E/PMMnPqdv/anNmknGxtNcfC7LisgkBjEUHGwdRuHlmIuXYm5PRoxyqoBE0ETcMOVR6B3vbR4qTCFr8bZzyBwGXHg2MtyC06H3rtDssqFoIsWEUCjYfDNCZahcc9HwNr/WtzZpNysrXVLM7uEZdVEGgsIsg4mNqDR94sXkKaE3J6tGMVVIImgqZhhyo5ttvBsI1O62FOT9aOB0faWALPVvX3dlEiVrEQZMEqGDR+Eo7eh6zCpbKKXZszm5STzVdzmdORCWIVBBq7w5+tKtBsKOYaTcgVOFZBJeJE4DTsMOVaqQBM8kujwTDG+tuOB0eq6MMvoL8vgfuStApCkEWcGIPGbFtDaGtzxhOMhcrKiiSrYKBZmG0Rqm2SvppvthfrVkGp235DcCJoLIU/C8HUaDYx5hpPiBVg7hlVLUwETcMOVa6VClaZPODEEoDz7Bf9jPHKYpjaEaD7lZVlB0JO2adhjNYaCLKIE2PQ2ByFNy9ohGHYteraxNk+QrUZT4QuYw/rtQUe2EegMQ5/FoOpxdnEmGs8IVYgcc+oamciaBp2qHKtVLDKJQAlZ89xNo8VD450Ll92mDFaOy/JE1AdqnrRp4yU+BFnx75wK6Q7yySyComsQiKrkMgqJLIKiaxCIpFVVMrgNGyySq0qQ9OwRdga71mxnWGkdpBVTGVoGjaCrT2t0sQ1QVZJThmaho1ga7xnRfJajWGTVeJWhqZhI9gaCZHXagybrBK3MjQN2w1bIyHy2hvDJqvEqwxNw3bD1kiIvFZg2GSVuJWRadgu2BoJkdcqDJusEp8yNQ3bBVsjIfL6jiAALVklpjI1DVuXAFtjDBuR194YNlklbmVoGjaXCFsjDBuR194YNlklfmVoGrYMWyOrIPJ6TLKDgGQVQ5mahi3B1sgqiLz2xrDJKgkoQ9OwQ4etySoksgqJrEIiq5DIKiSyColEViGllVUqvl1XlXlVp0yZinvHYRWTR27Y7wU+JSYUm6hHyeC3mASmatPLzogCNLu+9Rk+y0htUqLOkpyqMb+qDNrGXyFl0kdr25sBwkOl4HAB95aE8rixRNjaFfWdrlbhes7TKlx3uazyRkH+xFmv/THCb8Mpl/Gziog6YwlV+1iFCUQ0/gqpO24sz8pb8Q1jP6+4HYZtRC1AuLcklMeNhGBrOeo7LU9AnEfevWqk+a3FMGvIrq7aufxSiCxlmAtaU5Sv31udCAN8l0GrUaLOSHLVUdRSZz04aBt9hZSpNWxhJ8Ez7Ca4WWoBxr3dWu3xInYMW0tR3+lqFf7/dWaSpxhmbe6aITyZGFnlMrhOn/jKQIaUyyitglBnJLlqySqKoG30FVKmfvBReS6czUYaoXNCCzDu7RYK57aEYGt3BHfaWuVRk/kSw6zN3T4bOktW6WgeaGvumOO7jNIqCHVGkquWrKII2kZfIWW6AJ57CqBBRT/4WGoBxr1loXBuRwi2dkVwp6VVoKysRwlE33S6FPiAv0knDEWr5EG5qxvitQzqFClRZyS56qhiPThoG32FlOkGmH4GtIK3WsIPUgsw7u3VTbPDuR0h2FqO4E5Tq3CdttSacsKszd2+QXeJaBX9xCrWoFjGtSol6mxLrlpEndVB2+grpEyPw/kFTWbCBVkNXS1AuLcsFM7tCMHWclJ3up6AdnWyT5Ji5rW529/TkVKRYT7CPH/EWkZpFRfqbEuuWoQS1UHb6CukTPMgH87/PisPejLMZEu4t8dI0VSvp0VF2FpO6k7bvsqCiP5+KIYzr83dfj5cyjDDPAPKDJjvyXW+yyit4kKdbclVS1ZRBG2jr5AyrdOM8bJmZjiLYSZbwr095IRzK5Kt5aTu9O3WToGiVeIJxNntFXdHcr9kmGHe0QFO1jpgq4ZCzyq/ZZRWcaHOThdQqlqyirUeKWgbfYWUqSoXojvZjcYrNVALMO6NhPK4mTLZ2h31nXZW0Xnkk1lFDziw7BqUeT1J/7trPuQ8oY86OgwzY8vbQqTrQVnQ5j/MbxkkNeqMJFSNUWdl0Db+CqlUR/7Q0CKAhyQmW8K9kVAeN1MmW0tR32k7Wtte62PlAgxHmddGp76g81hzBNJhmDVtv760oFGfe3/Fo6iuZVwD456oM5ZTNUadlUHb+CukUgO0y2NW0wr+KzHZMu6NDpMoj1udbI2jvtPzBERKXgnh3rUIW5NVSGQVElmFRFYhkVVIZBUSiaxCCtMqdYA6UzB1WKrZXXtWEShq8TWCfLD1GfOdhbegCbS0zD07SDWiYaUUYDWp7KOdV7bLaz/1V/cE4mwRw+zKsrjO+vMPRxx39zVDPzM/FuOng5aoUqqHhJqgIa1U0vzh7bLhIv6XivBuzskXjk4FeMtirPaKFLVslXZ7BKvYEx5WsblnAan2sYoPqaxW9XF6FQOqXRMoCRoxzHJG9IJsa/8BrGbsiajONuL46WAl6pTqkK2CViodmsdCs57Hj1rA/1YR3iFaRUKdxVcHa7VP1/7JBtcEXoHAPUtINebWnIlYpLKnnoaiN/f8q8BgWdEESoJGDLOUEb2jw0nW13v1xWpuYD3nFcdPByvxSakON+0LrRRrPMzEH3gR3guzi9Yx9lVe7vJkT0AS6oysEslruNW2ijghWYX/3+CeJaRaZZVYpLKnToWpjNMtp7omUBI0YpiljOjzo6vRS7QXAexjcvx0sBKflGr9mz44I7xgOGelSMsjx0ifeBLek/kt8P5wQ9J9FQl1RlaJXsVZe9Mq4oSHVQzuWUKqVVbxJ5UVagP8rv584ziKJnRZSdBuhtnOiH5W26+iVdZ3h6bW39gQAUp8Uqr5N9V8MmJQWE4Rm+Poehh1eouiwfZJXEF47+4ELz0O3SqTtoqEOmOr7GyVvdy2ijAhnUwd7llCqlVW8SeVFYrqpK0Jl6EJXVYStJthtkrWNe5cIVpl/oHggHfIEEFKfFKqw+uoeDTHEd+IxQXQYKmwWg/Cm82FkuKs95O/ApJQZ2wV7Xc4yLaKMOGxYQzuWUKqMTgtTviRygrlAKfk1hvuQBP6hY+VBO1imK2Sqn7wX/HrzczVWr3HyxCBSnxSqiGr1GHEkxdujqPjIecptucsOMWcVhDejF0EcFkIF8sS6ixZhR0Ls22rOBPyCcjmniWkGiOO9kRMUtlTLYAfXT81zjlogstOgnYxzFbJ4xDt379/FvQ/Wz/hXg6QfauT8+oYImiJOqXaPAGFNG4iNUc8qkzSD+BOV9ab8NY6xZFfQrCKhDrLVlmc3cOxij3h6qtY3LOEVCusEptU9tJg/dnnGcbPGE0YJjUfRHUxzFbJndYlrG6vsdrB5112+0S3IYKXoJRqySoLF4RkFXmlDrp9E5yvH1kbOjNv9n51lAkiJ2kVCXWWraL1QAFcE+52mNyzhFQrrOJHKqv1f9BuI9vQxrg+RhNMTIKWGWYpI9r8ercDtFvBWCm4DBFHCUqp9u6VJS3XSh3Ick2udpatusT4pjLhnQKrIIp6RllZWRbnm+9hbJKOPX+zvZi7A00gSdyzgFQjcBpT1D6kss8Q3PFQ0DMfTqx2TTAxCVpmmMWM6Cf1r3eSdjSIAHTq06dPgf51xPjpoCWulGpbE/RvGtLb6aSVIquwewA6tIDGX/G/ZcJb6OqU6cz6IyEM7AsUtTxaC7CMPczdgSbco7XtHe7ZQaoROI0m/EhlH+2a0ja33VW7PCZQEjRmmFFG9J3WCWgfGlAV46eDlrhSqqXR2vHhWGWfa+RXfCDkpSPzG5+5wjg7SIS3IyPjRBpkT8wqsX7Miz5VTCSuDA2mxr/3dACnwxVBCCSyComsQiKrkMgqJLIKiaxCIpFVVCL2m6wSTAL7jVKaTZK800j9zp86qRtUEdwSY560EHnti2E7WDmG1MkqSUlkvz2swt3yHPNL6gZVBHfYVkHktQ+GLWLliEsnqyQnzH6LIZk6SV6x5hI4hPkldaMSHMEd6s1kTF77YNgiVo64dLJKcsLst2QV/v9NkMf8krolq4gR3OFaRRcirz0xbISVIy6drJKcMPvtskr11iv1n646qVuyihjBHb5VEHntjWEjrNzNpZNVEhZmv7FVDGX9j/kldaMSHMEdulUQea3AsBFW7ubSySrJbH+B/ZasUlravUMuHLpJ2vGupG7HEjgAGzHmyQuR1woMG2PlLi6drJKwJPZbDPQ2+yq7r+APVyFDuJK67RIcwR3uA4WIvFZi2Bgrd3HpZJWEJbHfYqC3xZ6utV4GoE7qtktwBHe4VkHktRLDxli5i0snqyQuzH6Lgd6mVapusF4zo07qtktwBHeoVkHktQ+GLZxB3Vw6WSUJYfZbCPS+0ojW7tYAst9gfkndTgkOwMaMebJC5LUvhu1g5W4unaySuGT22wn0Nkdrc1sO14vUSd1OCQ7ALocwczIQee2PYd/pnIAkLp2skozqA/udWpFVSGQVElmFRFYhkVVIZBUSWYVEitsq5UtX7qPtlJhqdu9HVlkyMALQ5PbOx+3FjKhJd5QMfotJ0dh4eT0jIquw2+QtErIqZf264pqv8xzaFG+BSVKmYaPaXNiyglQWJ9Qx17jVUgC2k0Xt2iDhJgd6byrUHBQCLijEiOO3C5vd89k7F0WgK/OyCtddUjS2Ysu02uRrFTmu2SGIA1pFnYbNVDHXuMQvTlsVc41ajQOwxSzq1FpFsalQc1AIeEqs8n2zBnroz3QwoByR5oLs6qqdyy+FyFLml4atLVO9r/y9XsDjhBEx5L7f6sQ1CwSxVJmqqeo0bGXMNVOTymgiRsy11Wpc4sqilr9DWMmBqk2FmoNCwEWFloY9FaYZzcnOr3ZbxfyVjGN+adjmMouguadVUCy0E9csB1PHtoo6DVsZc83UpLIbW1bHXFutRiXuLGr0HUJMw1ZtKtQcFAKOFFYadncwz9Avv6+yymzozPzSsM1lvtaTDt1WEWKhhbhmOZjaqSxSWhDtMtXjrp5PGrY65lpJKruwZWXMtdNqVCJnUUsbJLw0bOWmQs1xh4BbCisNuwB+UP6oTats0qFCdRq2scyvw2Go2yr4jO3ENcvB1GJfJXLIwTnQYb2rRJ2GrY65VpPKMrasjrl2Wo1K5CxqV38rpI6KelOh5rhDwG2FlIZdCN8x9ksp1+GCVXSc2LTKBn33q9OwGWT17n1oATT52m0VHAttxzXLwdSCHrtZu5L6ri+c7SpRpmGrY659SGUJW/aJuXZCplGJnEWNNkh4adg+mwo1xxUCLiicNOxD4UXryicq98hMq7ynU6TqNGz+I4rkd7pYx38Q3eyOhTbjmjFB7LUVxNxeU8o0bHXMtQ+pjCZixFzbIdNiiTuLWj4BhSK/TSU2xxUCLg6chZKGfR30rlRds5hWOR8uZX5p2Oikhehmj1hoI64ZE8RMjHg2B3s8uvLKNGx1zLUPqYwmYsVciyHTVolHFrW4QcJKw461qazmuELAUYPCiDj+qRUM5CeODbkKq1TcHcn9kvmlYSOrILoZlbjjmqNiL8aiAIe9qFl32yA4y7Ua3zRsdcy1glQWJ3xirl2ttktQFnXQq7hE5bmpUHPkEPDwrcIWtwXocGR74LUhnHiSDid3zYecJ/zSsM3w5zLzPZkC3SzFQktxzQJBLH7/zwEa9uoWhVbul3f5pWErYq6ZmlQWJvxirlGrcQC2mEXt3iAzwvOJYlPh5sgh4I7CS8P+eUafRtnFx929WcqvNkZrCzqPXcL80rCtwUnrLpJDN0ux0FJc853oqOp8/zkjOkXzDr7iB4+W+qRhK2KumQ+p7Ez4xVyjVksB2EIWtXuDjA/PKqpNhZuDQ8AF1V4aNv5lh5OGndZSx1ynWQB26M0hCIFEViGRVUhkFRJZhURWIZFVSCSyikqUhk1WCabMTMNWg7oIEVdh2GSVBJSxadiG3C9QRoi4CsMmqySgDE3DNu4KT1HdVLIQcSWGTVaJXxmahj2cPwixr8TrLfFcFiKuxrDJKnErg9Ow2eveGfsCIq7GsMkqcStz07AZOx0eVPRqLETcB8MmqySw/TMzDVu7vMkp+NlzPgcR98OwySrxKVPTsLnugBGqeS1E3A/DJqvEp0xNwzYc+579t0ysm4i4L4ZNVolPmZmGzfUBdK4RuicWMIkQcV8Mm6wSnzIyDVvXGHF0z7EKQsTVGDZZJV5lZho2164G2Zu9rILBdiWGTVaJW5SGTVYhkVVIZBUSWYVEViGRVUgksgopdKtQGjYpkFWcNGxj0HEem8r/OVeazRUyXftShlmj4U3EI7sGUb1DsxHQjFOSPvqTc1/Hv2pLIUccqzc8Jq+lpG5LqUnD9rWKHDJd+/ILsxZIZcQjywyzIjQbAc2iVf7dXtzjvlWnyirqDY+ag5O6U2EVnIb9oMmKdgVXkpkUMl0HUodZI1IZ8cgSw6wOzWYO0CwCbR81vuSTInsL+lWNNnmYRIJ6w6PmuDO8TaUoDXtHYR4/CC+Ebt5zOyHTdSB1mDUilRGPLDHM6tBs5gDNolX4izkcq/hVLVslxDRs1YZHzXFneFtKURr2aP0INgHu85wZo8G1Lf8wa5tUdvPINsOsDs0WgWY5j9uxik/VLquEl4at3PCoOXKGt6MUpWF/CD0Z23tA/naveSU0uLblG2btkMpuHtkqUYdmMxFolvO4Hauoq5a7F2G+4UW54VFz5AxvQSlKw+6hHVBfhwu8ZpXQ4FqXX5i1QCq7eGSrRB2azeUAzXIet2MVZdXyJg8xDdtnw6PmyBneolKThn0/TGBng0c8rwsNrnWpw6y5bFLZxSNbJerQbEN25rUuJ4/bsYqyau8TUDhSb3jcHFeGtzBwlpI07O0FzbdEvTq17sDo2pY6zNq4aDNJZRePbJWoQ7NNiZnXQh63YxVl1R5WCSsN29VQB8N2k9c4w1toUCrSsNkFcJJXp9YjZLq25ZuG7ZDKMo+MGGZFaDYCmuU8bscqMaqWL5ZDkWvDO8Cki7y2o7FTYhWUhq13oQA8OrVuNLgOhuB80rAFUlnmkUWGWRWaLQLNKI/7/rKysghntB+IVbWjUNOw3RvesQpqDk7qRl2dVKRh6+rmdf7dF/qbGxOQTxq2SCpjHhkxzKrQbAQ0i3ncU63Zro1VtTxaG1IatnvDCxn7YnOkpG5BdZSGTUpXhRGNTVYhkVVIZBUSWYVEViGRVUhkFRKJrKISpWGTVYIpY9KwERWO6XNRAUOzySpxK3PSsBEVjoBzvGeDhWaTVeJW5qRh67KocAScI8UTmk1WiUcZloZtUeEIOHcrYGg2WSUeZVQatkOFu4FzUcFCs8kq8Smj0rAdKtwNnAsKGJpNVolXmZOGLVDhLuBcUNDQbLJKXMqgNGxdFhXuAs5txROaTVaJQxmUhm3IpMJl4NzBsH1Ds8kqSShj0rAxFS4D5zYw6R+aTVZJQpmShi3FXMvAuWWVGKHZZJXElTFp2HLMNQbObavECM0mqyQhSsMmq5DIKiSyComsQiKrkMgqJBJZJWzNe2A1/+fnX8kqJF89BdCIe2XSaWSVWmyTCmh+QsdI9+glrif7xWDqeFfkX4IDsBXcc69zfjp3KmPbG/yndjdWMAwbo9v11CpMAJp9rIKDqeNcURP/EmQVFffc4A327zMZu/Ww2t1WATFshG7XJ6uogeYrofsutiAnutB1TEHB1EFXpISTUQli2FTcc+Fs9vZQtqfF07W7rQJi2LpWJ/VG7kywigg0Vw2A33/fEh53LYODqQNKDSejEmQVFffceSZ76FI2s10t54EHxLB1Weh2/bUKApq3tYfWcLn3ckUJfRk1nGyVoABsFfc8sfS/Pd6o7nzvhgWVtbmtgmPYDrpdj6ziBzSzz7Khz94wraKGk60SFICt4p639M0fW/1y0zei0L+6FrdVcAzbQbfr2cWyCmhmDwI0XxuiVdRwsl2CArD9uGd21LUDD3+n99u1uJ2CY9gOul1frSIBzQtyi86HXrvDs4oaTpZKzABsNffM2Lz8rcXPsKf/UovbKQ4MWwr0rn9WwUDzxhJ4tqq/Yu8mZBURTnYYZrmE2QHYrqBtQYMvZbnz2LvX1OJ2igPDlgK9659VENBc0Ydfvn5f4v2amUSsguBkIfRVLEEB2DL3LGhp9mpW8ip75e5a3E4BMWyEbtcnq6iA5qkdAbpfWVl2IOSUfYoXwcHUwYXgZGQVuwQFYLu4Z0EjtM7MWaf/cOqHtTkEFwzDRuh2fbKKCmg+l/9/2B7zJYpIUjB1UGE4WbSKUCIGYLu4Z+HKNHehdppqDufV6rYKhmFjdLt+nYAyUBUv8v/v3Vxfvx9ZhURWIZFVSGQVElmFRFYhkVVI8YowbFIwEYZdJ21KBMNG1HFA+cDJiG5WTzhKOwx7y5Qhz7049KqtCW+djLMKC4ZhY+o4oNRwMqKb1ROC0g7D7g4LeURZacJbJyOskgCG7UEdB5QnnIzoZvWEoLTDsBtyPm6z+erwhLdOZlklEIZtaJHH80Gx5AknI7pZPSEo7TDss+Alxv4pYgeJbJ3MskpgDFsKfw4kBZyM6Gb1hKC0w7DXt+m84usubTcms3XS3iqJYthS+HPgfpEHnIzoZvWE2ItMNwz740L+3aKvJrN1MuJiOQEMWwp/DiYFnIzoZvWErHTCsLX+brPezSHyShJbJ7OsEhTDdlPHAeUJJyO6WT0hj8KlE4ZdCBMqWeVEaJfU1skkqwTFsD2o44By4GSHYUZ0s3pCUlph2A2AH3w3GNd3iW+dDLJKQAzbFf4cRDKc7ACTiG5WT2ClFYbNToOJlWzvZDgt0a2TEVaJH8N2hz8HkQwnO9sZ0c3qCax0wrAZ+7o5NO3ZDJqvTHTrZIRV4sew9yWUNy3DyQKGjehm9QS6Ok0nDFvTutEtc1peuC7hrZMpJ6AMFGHYJBJZhURWIZFVSGQVElmFRFYhkVVIJLJKCNsLc7/fjm6d12bsGleJCI+Xo7cdJi8MjytDwNFs9RXDzhyrzG2g/9XwPeYDj4dtFREe9wkBR4x5fcWw09oqIsy5uSkMWlqxZCA03+ILj+OQ5FBkwuOxQsAtxry+YtgZY5UpUMq55r3d4M++8HgKrGLC47FCwBFjXg8x7IyxSjd4Xv/wOf5edz94PGyrIHhcbRXMmNdHDDudhRDxAvha//BrKPSHx8O2CoLH1VZBs9VbDDutjyzWjs+Hdfq/66BAtgSGx83PTyoLqQUIHldbRZyt/mLYGWGVrvBv/d+3oLtUIsHj5uetw9vaAjzu11exZqvPGHZGWGUyDNT/PdE6sivg8RRYRYDHkVVwoLc9W33GsDPCKhubwNWVrPIqaLpZKkHweMhWkeFxZBUHmBRnq78YdjrLQcQ1/bcImvZqAg3m4hIMj/9ifK4pN5StLcLjrhBwxyrCbPUYw05nbUBjr6svbJXbevQ3UgmGx8vlcdxkr5QFeNwVAu5YRZiNMGxS7Z53aROQyCoksgqJrEIiq5DIKiSyColEVlFo34o5n+xctcBKhKr4dl0VbZT91ipD9SHLvJLBb/Gp8dYY5mQ+9Y922l+FXeBOfc5lZ0QBml3f+oxyM6JDHxc9V9p44jDsEGGCr+cZtkKfuAWvNHkRhl17VuG6S7bKDGj94IdzzgRYwmd8oyB/4qzX/hiBYxO2Srs9yCrWSpMXYdi1dBTNrq7aufxSiCw1pqPWBjgAPtH+v6cINmn/rCnK1+8IT4QBPNPSQGS7wgK5MsxIQrb+qT6h7cDp2j/Z4LXSEEQYdi1YRf9nCIyTrNIc9J/c0To1cBlcp3/4FZQwtqMwjx/FF0I3FtMqD91pWSWS13CrYxW80hBEGHZtWWU2dJaschkUjbjnlQ9+1ic6GqchVnPHHO3/o+F+7f8T3FmFbqvwP0bwvQPRq7gxkFXslSYtwrBrzyqbIFeyyq9m7/NP/IeXB+XCMh9CT8b2HpC/Xa4Mk9dMvM0P0Z2tspdjq9grTf5bEIZda1bZAEWSVbRtefU5RzUEuJqZb71w1AM+Z6/DBd4VRoU9U5rjWIU9qx38kVXslSYtwrBrzyrvGeyrY5W1/fSTetVdvHfCjjAf8DF1P0xgZ7s6tW6rGCegmVO3Gx8fC7ORVeyVhiDCsGvLKufDpdgqv2TlfM//NV5sMAPKjPdvPKk/vbG9oPmWaDcWwCoLNT8dw2Oo+ceLs3sgq9grDUOEYdeKVSrujuR+KZ2AOsKp27SO4Eg4lvcHO8DJGxlbNRR66iO2F8BJ7k6tl1W4bKtoXWUAr5UmJ8Kwa0OTygDKyrrmQ84T/NhRVlaWxRnme7SJtgDRLqUFkK9nWS9vC5GuB2VBG+NNhPMB3J1ahpnsCXrdmopg7ST942+2F3Or4JUmL8Kwa0PGVU5B57H6tbA4Wrs3Er0oCpB1rPmqu+3XlxY06nOv9frIbl5vgMBMtjBau9ZYzzL2MLcKXmkIV8qEYdetti1me1YuLw+30upFn9ajczeZhERWIZFVSGQVElmFtP9ZZf2nn9ak63dJ57bth1aZCLAnXb9LOreNrJIyBYWgxwsDS2SVOrSKmyB+Y+rUfalvlQ1BB5nZvo0jtM0vpy8PXgkykcgyqapATV5vmTLkuReHXrXVXeL6QdnAOTaAodbhWEVJEFf/wmre2GZOrFv8A/t4XShOcSDouKwifi2VVS66YmExPHXfMT/6TySyTAorUJPX3WEhY4uh1F1Sy1aRCOI94n2EmkcPy4XGZ0I/3TR3tdJKji7WXwPN5p3ZIvfAofMYJ1Y1Pc3vgRu3KV4d0CTvoD/oaa7TtI+iVX8tKyyUzzQIgpZmEypg7KXfFBb2f9uwiti2S6wt85H8dX7OM0se8Z1IZJkUVmDIk7zWIazN0MhdEuQXxeLJSY1tFf0fgyCu7N27d5a1xJ8BWnTKB7iEO+UsgKLujQEa85IbtIluRQDTGBvQHeDwf7NbOkNe77+zmosBig+JQjG/Gf9w796QdzLfNPIZDUHQaDZUAZusra/EMojYNrVVfrpzcCFAVs/Jn/pOJLJMCisw5ElenwUvMfZPA0jwZ7JryyoOQWyvsQm8yNiu0XCm9vdMyHuomu29AprzHz7kzaxm1Q/lwT8Z29cOZul7/2Lt/3+HQu1UXD4CjrQPfn2fWTpPvsLFELQ4G6rgNSjSWvBRO+eYa7Xttt6Q1ZtrmfsLbdHmz3op5kQiy6SwAiV5vb5N5xVfd2m70V0S1CqR0oJol6nloVnFIYjtNbaExyq0k8VInsx6uBGoWNUUNnK05zZ9hul6V+Nufld8S37WCu3vLvA3XrCvvZkirZ2Uajy7gOXS6dSaDVVwvNGBmuu2is9vpaIvtBsJBYtiTCSyTAorYEry+uNCXhB9NXGrQOSQg3Ogw/qwrOIQxPYapwNkt+lzif7DL4AV+meL11UzVmhOrNDPrb80hnfZVDjV7k7oet1vh0oQtDMbrqAhrNSPPQ3jsUr5mVnv7jq4/QcxJhJZJoUVMCV5fZx2pdi7OUReSdQqj928hbHv+sLZYVnFIYidNf7r9JKIttMmMnuvWa1dYXY1dHtdBSf93Eh/3clugEPLDM3226ESBO3MhiuIwir908bxWEX7HWom/jn2RCLLpLACpiCvC2FCJaucCO0StYqhz6FhWFZxCGJrjf/td732iy5/Tu/J9oc7jDPnG9r/+umPYPLDjn5xtCkPfmeGQnfmvRf+pX/y3aESBC3MhiroCRx0ZO97nYCyQL9e+uyn+jQG5kVeN9CvOTeYz5smbJUlxjVU8lYRCWJrjW9CY+36oubv0EzvYeY/ydjeJ5sWruD98byHa1jNP/LAOIOO0g49xmHifmjBydX3jxznaxUZgnZmQxXcDw20FSzu5GWV5vC+9v1/n7W6fphETV6fBhMr2d7JcFqiVhn2ovaz3DYIzkrSKhJBfL3FLWtX/v/WDNDmsGKAG3jJtQBNyxpD9kT+poqbsqDRoY3MEsaWAbQ3rodrRgO07XUg6FdNN5kh0Se7VytC0Gg2VMG+AQDN2/KOS+kfcdv4Wa+grDXA8eX1wypq8vrr5tC0ZzNovpJ5MNnOcVoEzqUzDzTs1S0KrdaEMlprE8SjrE7lLdouhL48kOSgh4w5/zO4abTjpcuNif+deWDOAafPtWo5GewWzjq5OKfFWXpXeKxZV3uP9QoQtDSbUAHb88cCgA4ziwCG4rYxVv2XLnnFp/2nvpx6fMjrdaP/v71zAY+iOvv4u5tkN5sNhECUSLgpUS4BF4w2UlF4pFwsIAVsvwoURShY0eIN442qtYoW9Wu/rxRv1aJV8FZvaEE/1IJXUBREuQoBJKJYIxdJCEnON+fM7bxn5kw2yUY0vv/ngd0zc+adMzu/7MzO/ue/x6Qfc36Zz5zkrtYundgtGjn+8i+a/mFZq+otu9iOdV8e2Zfwm3Xbaxnp2xFZm0iEColQIREqJEKFRKg0kz6bv5R9On8Z7R5CpT4tgV+x51IYskdqsagsghns4cYkl9UdpF16hFDZ1717B4B7zYaIhQhl9bxit9IYJ64EhlsNWGR2/GRyQaTjlK08KRXlA8vVKtBvACqaD9eyeda3jlj7r+wc6VJqZlmoEc/LJ3ROg6liq1BIxMZzcmMl4lt6daCklL6r/Bagj4QKV4dduDHO2TWP8n7LssXzVq8xb5S0Uy0QlTlwG7sV/uKdUXuGWGpwrQmEHPF8eAq07TvovBUyKuKb2PVt8nvkgwgUVQZKSikqh9rBBOE2Eb2jtYcrXusHk9QGpNXVHNw0SWBQngvD1latGQLtdnuipJVq/qsc7OzQp9VZD0N8ceVzMXiEN3DE8wyY726VsE7MMg5jjH/5apD9j1BkK1MGSkotKo/BaZut3W3v3HeFhRY1zH1TJjL3ZkER/yK5uidc7YmS9qnWIFRGQinj/uuRzhQ74nld+DS32wSO7+F8WM0b2fz+B3Yu3MiUgZJSi8owuI/9KG2nvHM3mJYpuWHugfuhF+NhWKY75VHozdQoaZ9qXo3OiUCW8S+es1id1RG4X2W5e9uKE/E8G847u318uBSi9az1fhMXXzZblg55oKSUorIjHNvL/mScZbo795sJ/Et/3OC2lt75EOX7NmaZrDdAFlOipH2q+Wqq8X5yrnGO41FU2G4/cd4U3Ihn7pfIi0G2+4sHZ8M88Tgc8rezRRmmJ08eKCmlqPweYonE8XBUldk7VFzcJwZtNqgN83gxSuynTDB9jmUQ4w9ylLRSzURlaMKz1jGw3NjDPr9zkQ7lnDgbFSnieRCkP8Qqx8JP7a6702OmTXV1DKK5xuhGMmWgpFSiUnesddbwd2a90OHMbr/e4mkY7+sHull+vh7wonh8wXyXl6Kk1WomKp7bbAPOVdoDvxdmlXkAQhHPo+FS8U5mm0zZ7U4o5HtDs9r/fJpIREcDJaUSlWWmU/9ty0eNDhlyg58CrAjD/UycFQwR0840Dw1SlLR/NQ8q0xNZ0CsRgd6JV9TxDBf3/swx3yFQxPNNMF683zjO8x7OJy2umhPNc1x5oKRUojLevBeMFZr3dAaiYnz0ifPbLT5tA9ccYoeugtxyMc+Nkvav5nPzfm/43HgDqfKO5+/Q+VO2s6P4sIwjnrdmhJ9kNdMdP/IbUCjdjvbucLjAM1BS6lD53PjT7macSdyXyIVjLuV5z9wQbd6NLjeu5G7ts1hVbzg6YZyyvhSH3H5tINv6us+OksbV9ln26kQiwzuGTlDJolk+I6odBLG+mXBmrTfi+S6Aru0h52Or5wXO1b0rBp+ZD3DuIe9ASSlDZad1cfxm/jDavkB7WL50KxrjLJ/0auMsc4LR3nx+h4yCyVvsMlaUNK5WgX74T1GrDFYJHf2GdGBWp4zOVx1gPhHPj5+SmTNmvd0vO63cPp4B5J9jGrfVgZJSeV3lSKg21JaVw4kpqlaz9zDt55aKSvXce1jF3IW0dwgVEqFCIlRIJEKFdCRRIRc1oZKkyEVNqCSpRruoSd9bVLBteYR8fXVEwMVWvYtajmvGLmrXK602ZOc1aqAB4KheuQCKkkbp0aqPm9RUVGzbctKo6FzUOK4Zuaglr7TSQM5rxYYtma0RKqiAHCWN0qMVHzepaQcgbFu2kuHMd4aG201wXDNyUSOvNGog5zVqILM1+tIbFRCyoqRRerTi4yY1FRX+v21bNlp3z5VRmTenIaj4xDXbLmrklUYN5LxGDWS2RqigAkJWlDRKj8YjIKUEFdu2bLQ4GROH2TvHaJkNSQEuaqai4riokVcaNZDz2mPDdszWKNUZFWBylLSaHu2OgNRUVJBtGfC5hu6HIrQuahUV10WNvNKogZzXqg3bNVujVGdUgMlR0mp6tDsCUpNRkW3LAEXpLiqhIqklS+uiVlCRXNTIK40ayHmt2LAlszVKdUYFmBwlraRHSyMgNf0AJNmWrQPQ/NKv3AOQRwEuaoQKclEjrzRqIOc1anC5ZmuTOMtbi6px2VHSKD0aj4DU9HMV17ZstFau4D+4sM1CZeUK7yIBLmqECnJRC9leadRAzmvU4MJma5TqjKrZUdIoPdo7AlITT2sd27L1YdlFxV9aF7WMCnZRi88itlcaNWTnNW4w2WztSXV2CqAoaTk92jsCUqNRwbblS0Q2tqG4gYppw/YPMtC5qOW4ZsVFLXmllYbkvFYaTDJb41RnVECOkpbTo1UfN6nJV2u72LZl6QLtNqsxw3cxnYtajmtWXNSSV1ppSM5rtSGbrVGqMyogR0nL6dGH9ZebSY04ADVGjXFRI690k43T5Lz+nqBCLmpChUSokEiEColQIREqJEKFRKiQCBWchi3/Ap75q4aR/OEvBC8T4KJuVEP2cSM7LbJUI+e1vgCeQ2rqu4qbhu1FheuOwGUCXNSNaKg+bgkVZKlGzmt9AbQeUlNRQfnV6AfS02pr9q+7EMJrA5fRu6gb0cA+bvTVttdSbTuv9QXQekhNRQXlV2NUxMMIn7sI5WX0LupGNLCP2+uCQJZq23mtL+DN1iY1ARWUX+2DyhIoDFxG76JuTIMhVGTnNZdsqXad1/oCXlM3qfGo4PxqH1R2QUbgMnoXdWMaGBXZec0US7XrvNYX8Jq6SY1HBedX+6CyE+KBy+hd1I1poD2NnNeqpdp1XusLeEzdpMajouRX+6DymhlbrF1G76JuTIN57jmznddeS7XtvNYX8Ji6SY1HRcmv9kFlPFwYuIzeRd2Yhg8qlvPax1JtOa/1BTymblLjUVHyqz2oVN0ZzvgocBm9i7oxDbSnZec1tlQj57W+gDqH1GhUlDTsOYlEIsRd1HcxdqmwZPfIhPQHA5cJcFE3oiH7uJHzWrFUI+e1toDH1E1qNCpKGrb3am2scMqa4GWCXNQNb8g+buS8VizV2HmtLaCYuklNu65CIhEqJEKFRKiQCBUSoUIiVEiEComUelSqPimroZeQUBFClmrFX/3hz6IAbWcX/ExdSHZEK8u4JmgraLvbpNVqQ98tIADbm7h8vfMMma3lnGwUjS2tNMBjTqgESLJUo8bzscyZTz/z2zCc7qmJQ6bdZWQTtBO0HX5Uaei7BQRge1BZkWY/Q2ZrlJMtR2PLKw32mBMqGiEbttTYGs8UXzbPhMHqItgRLS2juKjT6mqqtk6HE9RGYDf+v18ANk7qZnu7DrW/BEdma5STLUdjqyvVeswJFZ2QDVtqXATXi8ePId93OccRLS2juKjFvt0FEbUR0E0fgI2Tutn46GYbFWS2RjnZcjS230p9PeaEik7Ihi01jgPzO+W625f6LeY6ohUfN94dtZ9fab77oIa+m5BvADZO6n7EYMFGBZmtUU62Go0dT8ZjTqhohCzVciMCFQGLuY5o1ccdV+/4Cv3b09B3E/INwEZJ3WU5hVUOKshsjXKy1WjseBIec0JFJ2SplhviOK+T5IhWfdwyA0VFvbpmQJ9dakPfTXxu8Q3AlpO6awbAS65hD5mtUU62Eo3tRcXHY06oaIQs1ahxMmjD3mRHtMfHre6Og5fDCLWh78alCcCWkrofgOjAgQNDMFBAhM3Wck42isb2Q8XrMSdUdEKWatSYAwkzFnZBmbqQ7Ij2+Lg9u2Ob+MtFDX03Lk0AtpTUPRfkz9Fes7Wdk42isf1W6vWYEyo6IUs1auztCmcZ54ObRkNf5YotckR7fNzq7qj5HfRWG/puTB+AjZK6zXMU89FjtnZysuVobJ+V+nnMCRV/IUv1NMVfva4ThHscG4KO/8ILyY5oxZMtm6CvNKO1e2ZD2vO4oe8m5B+AjZK6+XudcIwPFZfgZLM1ysmWorHRSrUec0JFJ2SpHqr6q7+aXRRrXfLfajqJ7IhWPNmyCdq6DJtxzISVSkPfjUsTgI2Sug3NlfI0ZLM1Dtp2o7HRSrUec0LlByTKySZUSIQKiVAhESokQoVEqHzX9Nn8pezT+cto9xAq9WkJ/Io9R14RQqV+LYIZ7GHfn8o+vH7pO/s3rThoNVNhBK87SBikABWQLauyf9WyR7casIixjVGAnrx3D4Do455sjX+w9aJ5M/JkV5h9/H8ncz5cy+bBLd4Z93Y2lsnqDnNFCxnB9ZnXqMFkgzZjyyd0ToOp/BmK0yalGhWuR81uqxhbaXqLVFQ6V9qoMMmTHYjKHLiN3Qp/8ZleMO/NpWPAdOAhI7g28xoHYDPZoM0OT4G2fQedt8LcVGQeJzUUlQLLwmr+XcpWZ26PPrhpktjzED3hEsYu7u7Ef0VlExt/d0gzJyBTt+6HmvU//34UvGP8XxnnXmrFCK7NvMb+amTQNqie70z3xmmTmoaKY3U2sSkT5jKI3pxXXZ13qx8q4Uirzx1UkKm74ai0A/FHf6pwNGAjuDbzGvurkUF7Xfg0ZQUoTpvUNFQcq7OJyv3QS+z0baFnnwnv8EMlehX/KGOhgjzZOlRG50Qgy/gXz1mszLkI4hPvevIN0y+JjeD6zGvckA3as+G8s9vHh7vfH8tx2qSGoTJumrVHp41zTl2cI0si0TsfoovNnX7GuLGDmS8q+zukrbNQwZ5sHSqMTTXeT86FVz3Tv7Hu+7uM/+ljI7g+8xo1kEGbV8uLQbZ9sw+K0yY1/MOytEclq7N1xjtqrdXlvmj0IX9UjD/kYRYq2JNtFR6a8KxyjHGgGQ5+t2stv+YXP2oFwhqLjeD6zGu5gQ3agyD9IVY5Fn5qfe5CcdqkpqHiWp358wPdzNuteJevM+MHNKiw02GJQEXxZFuFCyDZc5VtA8RNPjV3iLMTbAQPDM12GtigPRouNf7fYHprvXHapKahYludxbnKijDcb3d5ZBHTofJBWm+BiuLJ1qEyPZEFvRIR6J14Bc/YF0r/jDmHGWwEDwzNdhrYoH0TjBfvRMLy7xOnTWoSKlwOKmwWxDdJXTSoGKej4hRH8WTrUGGsN3xuvE1UeaYfByP3GB93J4krKdgIrs+8Zp6wdXt0WzPCT7Ka6cKGjeO0SQ1H5fNEAkKJxAP8uWx1Fvbos1hVbzg6MUN02bZFOJ+XqKHZxqwtX+UZqGBP9j6zsKEM7xg6QSWLZjGf6RDtXhSDzDfFZ13ZCK7NvMYB2Migze4C6Noecj72xGmTGo7KTumiqmx1FldruzC2mp8KiknrPxQPC31Csz9k9xioYE92BXgSUVy1ymCV0NEzuTocnRoFCJ1uWbKREVyXea0EYCODNnv8lMycMeuZJ06b1KgDUCpU++6qhvQOtWXlcKJ3xp4PWOXGdRW021owKg1T9dx7WMXchbR3CBUSoUIiVEgkQoXU0lApe2tXCruRWi4qNYWhNanrRmrBqDwEYzRzkIta343UYlBBXmlkqeaq7WHmLKleaddFHdBNtWGnbqDyd49eOXbvTbNOGjK419Vf+rz8coHv+AXj78iwsFca5VcLLbSSlbBXWnZRa7t5bNgpHGggKq7du/SCLxhbAP2SRKUNoRL0p4q80ii/WhxkeoP5xQD2Sssuam031YadyoGqPj6AEue5bPfm2uxEzkndUAEU9U2oBJxwuHsA5VdzPWEb1YRsr7THRe3bTbVhp3KgQahIdm+hp2FYPaigqG9CJUDuHlAt1XUJeNvt53ilVRe1phtLJSq4Gs7jRqjIdm/+HrOsMPy+DypKATfqm1BJag+olupnYKjbzfVKqy5qTbfmRAXlccsMILu3dR5SsteLilLAjfomVJLaA6qluhhed3pJXmnFRa3r1oyo4Dxu4wj4hGXxxHZvQz8+uTXAFLWbp4Ab9U2oJLUHFEv1Yvd3ZJBXGrmo9d2aERVTdh63JGz3Fqoq1X60kQooUd+ESj17QLFUl4DzUwzIK41c1PpuzY6KncdtfFhf+LL5BNu9TfOU5x8AABoxSURBVJU7qDjdPAXUqG9CpZ49gC3VS2CQPQN7pWUXdUC3ZkQF53HjT0DMPQA9ePEmxg7OsO6EkbupBdyob0JFI+yVRvnV7DSwj+yqV9p1UQd0U23YKRwoyuNWUZHs3v0BuvTJhg7b1G6eAhdooiEIFUeKV1rOr15m/nYPl8cr7bioA7qpNuxUDlTO41ZRkezee2/pm5XeZcZubzelgBT1Tag0XAPh5RR2I7VcVJZD/xR2I7VgVMrf353CbqSWfAAiESokQoVEqJAIFRKJUPGqEQnaFWs3UvDkDw4VN0H7ZjMNZK75ICd14wxvtmZIGKDNbYVnVOOYZyQr0DB/+AtG4wbR2GPljYyAZjRYexzr/nP03QgVjaQEbYwKk5K6cYb3y1lt73rv/6aGoQerFxWuO75NVLyOdd85+m6EikY4QdtytvokdUsZ3p+1zRbfR94CZhKmnAiOX8q02pr96y6EMHflWT8gbn23rF2myfI41v3n6LsRKhrhBG2MipzULWV4l8INYtLetMxaa7c7ieAKKuJhhPhxGg8q/ss0WR7Huv8cfTdCRSOcoI1RkZO6pQzvXmDdevbE6zYqTiK4HypLoNAPFf9lmizVsa6Zo+9GqGiEE7TRKQRK6pYyvGPwBX7FdOccFiq7IMOLSrPdM6g61jVz9N0IFY1wgjaEirjAm9QtZXhnwXbG9omOJ5m73U0E90Nlp6BERUWzTJOlOtY1c/TdCBWNcIK2fADCSd1ShncfeMz+9aKodDARieB+qLwmcpzTQvzazSHxDhOwTJOlhoBr5ui7ESoa4QRtGRWc1C1leF8PxYeU8w4nEdwPlfFwofF/PnBz3gf23ci6ZZosNQTcdXijOWo3QqVe4QRtGRWc1C1leP+nAwzZwA8sGeiDrwaVqjvDGR8Zj7+Ei+pYzfkwOXiZJksNAXe9mGiO2o1QqV9SgvZ9PI77AbaA53r/CSV1yxnexltDJ4Cup3QBsb/lRHBU+FIxo0cmpD/Im5taQ35xHrTdGrRMKi7BIce6jAqao3YjVJKQm6AtX629GiV1yxnehr6eU9I6Le+MO8sZTgRHdc1lYoVTrFuot5xfEOkweTsLWiYVkh3rCBU8R+lGqKT6T7ZBGd4tXYQKiVAhESokQoVEqJAIlSOrz+YvZZ/OX0Z7kVCpT0vgV+w5YQ8hESqBWgQz2MMp+xHtw+uXvrN/04qDDZlDqHxPNB+uZfOEsxFL/vVEhn4jUbpwujEK0JP37gEQfZyxezsbE7O6w1xPOTznk8kFkY5T+AX7Cul3HbENW9XGc3JjJU8SKkdOc+A2div8pXGoiDmrGFtpuo7mQMG8N5eOAVjjXYs8Z1m2WLrVa76oWDZsRevb5PfIB/gzoXKEpP2RdyEn3ZH7AMQXxaazVXJEQ/SESxi7uLvoehS8Y/xfGQfPj8SgOeW5MGxt1Zoh0E4kL8gRxciGjTSKL/qPUGQrofI9Q8V2REP05rzq6rxbRdd2IBL5TzV/LVwWmjMLivjNY9U94WovKuJhhPc8Oxv4r/2eCzcSKkdGo3MikGX8i+csbhgqtiMaottCzz4T3iG6XgTxiXc9+YZPaCya09Oy0T0KvTWomDZspDi/8YxdYeUbEipHQFON95Nz4VXfeQ4q46ZZe3SauHFDckQbU88YN3aw2fUb67z0Ms8dp2hODDaIiRsgS4PKLsskKWk45G9nizLctDpC5dvWGOPQMBzWBqOi7FHJEW1MvS8afcjuuvyaX/yoFcA13lrSnEwoE5PKICYVHppgig0baXUMorkGbSMJle/0uYqKiuuINqZ+nRk/ILpuGyDu0am5Q9xLJgvP6QEviqkvQC+psDi2IRs21ntDs9r/fJofhYTKt6HpiSzolYhA78QrDUPFdkTzqY8sMrvuC6V/Jq6aeO5/wHOugCFi6pnmlT8fVEwbtlc1J35Xf73lh3AA6g2fs/ZQ1bADkOuItqeKrsfByD2M7Z0Ep6uV0JxP28A1h9ihqyC33BcV24bt0bvD4QK6rnLE1AkqWTTL59JcQiRRJxJ38dbnCeGqfoA/lxzRK8TUbVtE1yVGLYh2L4pB5pvetchzXopDbr82kL3MeL8xCxvKANWGjXTF4DPzAc49RKgcMbXKYJXQ0TsdXa01zjPdi6rS1dr/E/+v/1A8LKwOR6dGAUKnr1SLqXM2n98ho2DyFuZcrTU/U6k2bHSoBMg/ZyljhMqRUm2oLSuHE1NTbM8HrHLjuooGzUlWNXtbQubT9xmV6rn3sIq5CxmJUCERKiRChUSokAiVI7huclETKkmKXNSESpCkLOrUuqhJ33NUAMf7ulnUTO+iZuiHZ3fPGvHoY6Ov+tychVKd37pME+/5m5PPuPPa0e+5E663PSqomvcHbusvgEaAx6bKWQZVw3HH+pRqtHFqN6f0plknDRnc6+ovWxwqUhY107uo8S7sxd2GH0CRaMipzi920eY2Amxm7MHocru9Is3uiKrpUdEXQLnSqJoqdxlUzX452niqycIbp3ZzS5de8AVjC6BfS0BFtiuiLOoAu4nsLTNDIcutH62WU53fypn+juYHlJ96rJZ/RXOS1dzbdaj9RTOqhtaTZAGUK42qKZKWQdXMr7ZnGQdfFpBSjTdO6SaVFtps/4B9y0EFZVEni8pYeJyxf1o/Zy2nOtcdDP6t7XcBrK9exkc32y8tqqZHRV8A5UqjaoqkZVC1CZOM/w7nm+YVbUo13jilm1r6aRjW0lBBWdQBLmoIF8Wi3UvNb+h2dCxcv6F7p09FQ011DkBlRy/INZ89Ajc69hVUDa0nyQJoBKgalrwMrsb1rPUWE5hS7W4c7qaU3rusMPx+C0AFmaBxFrXeRQ0QPuH4dOgq4ojfzhKRsU+JOWqqsx6V5UeD9eGqLKewynlpUTW0niQLoBGgakhoGVRN6GyY57s9GlRQN7U0H0LJ3pbxYdl5n8dZ1HoX9d9+v5ux7f3hHN4wzujaFreDsLiPU0111qIyPwNgVCV/VjMAXnJNcagaWk+SBdAIUDVZeBm5mvnJKT32NfPbHg0qcje1NPvxya0BprQwVFAWdbCL2tBqaMUfsuCSQ+zQTOjMG2qqswaVuosB0v5QJ54/ANGBAweGYOA5nmpoPUkWQCPwqcZ8lpGrCd0OE80ngSnV7sbJ3XBp81pVqfl5qgWhgrKog13UhtaYnyyyRYLnTvMkX0111qAyxfhrf4XdNpM/nwvyZ2JUDa0nyQJoBD7VTOFl5GpCPezfHQpMqXY3Tu6GS5sqb3Go4CxqvYt63GMGUXuGwVjeGAUzD7Fq69Y8NdXZH5XbADqvZ6xImmm/YaNqaD1JFkAjQNW8spbxVHsDCut8qulR8Xazh/PgxZsYOzjD/0PY9wwV2QQtZ1EzvYt6NUCrfj2j0EHcDr6hHeT2bQvtNoprUVKq858TiUSYm6rVi3g1YYBuJSUlMXvjFgir9VC1Gl5PkgVQrjQamyJnGU81doGVnhCQUo03TukmDac/QJc+2dBhWwtARTZBy1nUTO+iZksndotGjr/c+h2essnHpB9zvnlTn5zqXGq/E1+nLH5Y/anAudIbtlwNryfJAihXGo3N7xhU4FPtQHZaud1Ll1KtbBzuJg1n7y19s9K7zNjNWsYBSKOUuqhJLRkVclETKiRChUSokEiEColQIREqJEKFRKiQCBW1M9j/O3HSYLfdf+pc3NPbX+5R/xx1Hp7j7aX29J/jPwL9MsHr0b1Outegvu33e139RxBUzX/PESqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKoUKoECqECqFCqBAqhAqhQqgQKoQKofIDQYX0QxahQiJUSIQKiVAhESokQoVEqJBIhAqJUCERKiRChUSokAgVEqFCIhEqpBaAyo5Vq+poHxAqSWgmQCXtA0Llu4pK1SdlNbTjmxWVChCa06D6I/gioayeV+z2m/t8aenhxo68nVE4yljceChowGIf/iwK0HZ2wc+Sf4lcNeSFjSb76jS4tEajRZ1I/vAXvseocHXYVV/Xsg++YG+X+c2p3cfqnt+TElSej2XOfPqZ34bhdL+55R/vbRIq0kCPFCpcd3wHDkBo+18d0z7j6NGvMrafD+8GY8p/GY//5V2m9nDFa/1gEmM38L1b8z+JrCx+BKiUX6LaOzoYjVPzYBtvPTW4TeTY3+wUc+ruPzEDcsbAALXwyrS4AdbHkYx1eDhsgmCIneezA7bGM98yj3yDjcovTegcOfonz/IJ4wB63nEcQMYFniMiJ1EULAC8Ccp65IFOt/faW/W9omniwSjN0YeH2RT+cB1+DeSVppkv2kz+sN5TrLZm/7oLIbwWb1zQMt8GKr8DiPeMc0YOngRw0j3r/nfPtSdA9rX+y7wL7Ri7p7gYImfxAfPDzqHi4uKQtf7asUa1XjkAOfw1/zVA3glRyPuIz7oaoH23TIDpntFcASMYGwi/U4bDZheLdd5YfLRn6y6C68Xjx5DP2E0AmX3aA/zJmHBVMR9WQVEELg1ERd4EZT3yQBuCyt1zRenBvYxX8UV2cyFEiv+KXwN5pSVdIFbM2J2doE1xmT93I2Aa3rigZb4FVJ6CyPxaVnt3BP7J2HGwik2E/2F38kH6LrMBWtnv5v3/sfZV+yNy1Fr/fIjcXcuqL+dAsb9C1pPG8W4inMJntYHHGDswGcZ4Kh/sBo8/AD0PeYZjj3OmZ+uOgzXmW9XtSxnbPmppNaudDsdaIxv2rvHexBkKQAVvAlqPPNBbiyFUzPVh/ajwshOHMXa4MzwtaP615zWQV7o9krbBmDAU/qZ7i1oChcrGBSzzLaByGtwqHm/hR/3fwA1VrWEgOxOe9F/mmwkw2trk6+QrKTYqJ8Ef+ENNLnzKWHf4X9443AX49h0Df6syjhyT/u4dzjLIzwu97h2OHpUIVKiT3rBeYCji/1dDWO0wbppVcNo4dRPQevBAkzpXsU6ErOd3QgljuzND/BiBXwO00ovgHMbegq6Hdajsggx14/TLNDMqQxOMZVlHvfWQzdhzcPKzAOFNGWkV3mWMP68+MWizwe8VtFGJWdU+KKu1zmGEnhU7H9I6lkxf6jeeqQAXmc/QcPSotIIdUuvNnxekZ7S3dhUkmHlcr+/IKz1H68EDTRqVonR7jfty4BVWCiOd8zjnNUDldmWGVrHhcI/2xGcnxNWN0y/TzKjwt8y4tW8+5uM6EAn9xNiqwfBj37+bcGa3X29hQai0go3ucQWgT8LUEt5+7uz8sFFkpu+HsvA+8xkajh6Vk2Gh2/hnGsQLj2+bKlTwQJNFxTwAzS/9ip8wwdCvW8NrPq8BKjcTfrISOh/SovIadFc3Tr/Mt4DKALjFesfnZ/xnGi/Rz41/N9Z3gUGDykC4XTzueN74r1CcbzBW9R/jv5cGzDZOLSoeFSe8uhdHHU46fMNPSEZ7tm4OJMyXa0EZX88d1YytagIq8nqUgYZAXOd77z9mz4ULX9ahsnIFP37yT367IsaLWCymo9dAGcDuLDge/qp/NcbDherGqcvohtMsqPwTIvfUsbp7I/CUMeWPAJ22+p/wJ4XKM5C5wDhRWJCbZbw5/Bnav2lMe/0Ufo68GHKMonV/hbaBqODhdDBOsdkn47wflvd2hbOMs6FNo6FvjXHQWsHqnjmmCajI61EG2g6Mk6g1vwxttt9bS4I+LJuoiM/d5tseeg3U120WQMcqXbGqO8MZHzFl49RldMNJESr7EgkIiffEDL7UTSFo3ac1mJ9V1wJcxooh13PF/BJzGXP6TVaFs0RrtvEsBMZ/9xmN6wByEzmQNtPYnLrJBnj9jgbxWeJF49XreGKetR6k+UY5MBdXhvNbgOP68KsJRZcpy6zrBOEex4ag47/4X196ooCfDiSGsisTkJmYz0p5xUuUZT43h/2AdxPk9SgDvQpivPigiqB9c4nYAENxE5UPAbqYZ57oNcArZWxPzDzpxbpUFOuRCekPMrxx3mWaGZUK5bLlv8ccnX7U2cvMmR3hDXYrP832vx5pbv8Ua+kuzP4TErqZt/41PDd63IXm1TT29Fl56e3HihPEm6B/Z6PPsXd7B3SDtDgezsHLIwAdSo2Z49SFvppdFGtd8t/8wLF/qrGXT71XXO4dZ15G5BcR+dUapJ3SZWq8CfJ6lIHW/rF7JG/Uv4L3jXS1VqDCzoK77HnSa4BXythHoWMqdVdrY4VTxPUAeeO8yzQzKkdG1Vt2sR3rvmzwYht3JdHrwIflTR2es55GDrQxmiIurDVs45Jb5nuNCgnrnuLi4kj7g82+DKHyvdcNjfg+8IaUfIdIqJAIFRKhQiJUglT21q4UdiO1XFRqCkNrUteN1IJRecjHr2Kq7mBS3UgtBpW3Litwx7L/ys6RLqXfuHNre8Bq/vibk8+489rR7znTl0/onAZT6+mGSqd0oNZVVI2393r7C5hNs04aMrjX1T6X51CBFPpsWzAqL3aRX6LaM8QrNrjWmb8QRlmv5mbGHowuN6cengJt+w46b0VgN1w6tQMNRGVFmt2x9IIvGFsA/ZJEpQ2hEvSnmjP9nbgzlochvrjyuRg84hxkesMq8eSpx2r5l1EnmZNnwHx8LPLrhkundqCqKUX+amVv16FRebWbhelK6YYKmN8yz4IZhEqA+AmHuwdGQinjLuuR9oQn4KdS53etrx/XhU/DVXy74dKpHWgQKuOjmxEqT8OwelCZMIm/U+abx1BCJUDuHugI3Kux3Hljr0vA226/Hb0gVzyZDeed3T4+3PnIo+nGUokKrgbholi0e2mFDyqPwI1MQmXvssLw+z6oKAUYe9Z+yyRUktkDUeGA/QQyrfYzMNTttvxogFniGf/WPS8G2WuDuzUnKhA+4fh06LrDw0BZTmGVjAo/BynZ60VFKcDY2TCPUEl+D6QD//p8h4NKMbzu9JqfATDKdF4MgvSHWOVY56ij6daMqPzt97sZ297f8ek88cQr1vWdAfASk1H58cmtAaao3TwF2O702NeESvJ7oD3w9+pV9gFoMb8V0DrGXAyQ9gfrNofR4vauDfbpoq5bM6JiarV1j5OkByA6cODAEAx0zV5VpdqPNlKB22EiXVdpwB4YLr4xn2Of1pbAv+05UwDavsJuM03xN8F48ebTKrhbs6OyBlrbH9Ztd/Nc8H6OLndQUU3QbgHWQxj2CZVk98DfofOnbGdH68PyEhhkz7gNoPN6xorMnlszwk+ymunWtRRtt2ZEZdxjhxjbMwzG+nwCYu4B6MGLNzF2cAb8wtNNLfAGFNYRKoH6cyKRCHNH8l/EJbhBEOubCWeal+BOA/vIXhMG6FZSUhKzRn0XQNf2kPNxYDdcOqUDXQ3Qql/PKHTY6oPKAmEyFyfa/QG69MmGDtvUbp4CFzQwaOIHiEqp/YZ9nWgemNUpo/NVB8TzZW4GwmH10vfjp2TmjFkf3E0pndKBLp3YLRo5/vIv/K6rzHUPQHtv6ZuV3mXGbm83pcCB7LRyQqXRGggvp7AbqeWishz6p7AbqQWjUv7+7hR2I7XkAxCJUCERKiRChUSokEgtHRVdsvXh9Uvf2b9pxcFvdaWEyhFRRA4g1DXcZGszP2OPHW5xL4+vyOoOc5knofiTyQWRjlO2MrbRWLYnn9IDIPo4ShsWCRoQbjVgkc/IUJy2Uw3nfnvjl5PaHkKlwZp6+co8eOhPp30Z3JCSrTEqc6Bg3ptLx4BIHsWoLMsWz1q9Zs5ZxdhKc4YXFa5HPWNDcdpyNTn3W0Elye0hVBqsryPWi35fYAMlWzMRF2h9mXsUvGP8XxkHfqchip0tz4Vha6vWDIF2u/nUEy5h7OLu1hfAbjA1f15Xc3DTJOijjg2tFFVDud8oqTvJ7SFUGq7/zB2eZbyX971iVWADJVsjVNqBuK3jVBFNh1CZBUXcml3dE67mU2/Oq67Ou9VFxQqmtrEpc3x4jtBKUTWU+42SupPcHkKlMdptnGuEHq+ngZKtESoXQXziXU++YToPESo9rVS+R6E3n7ot9Owz4R0uKnYwtYXK/dBLHRlaKaqGc7/lpO4kt4dQacxHjP7QeRLE3g1u4GRrGZVvrN87uYz/zaMw65gVLr0BssTUM8aNHWw7kGR3Aw/g650P0cXe021ppaiakvstJXUnuT2ESiNUMSb0yoHju7wR3MDJ1jIqjC2/5hc/agVwjT3Tvt0mE8yfIyiDmJh6XzT6kIuKG0xtnkGMWusZGlopqqbmfrtJ3UluD6HSKL3N2Pqv62mgZGuWFuLXOg7xePltA8TNWTV3OD+t4KDSA14Ujy/wQ4sx9evM+AGGD0AimNp4fqCbY3CUhFaKqqm5325Sd5LbQ6g0m1CyNcsXyekf8DOTfaH0z8RFD+es1EHlChgiHs/k9wjxqY8sYhIqTjA1P1dZEYb7g1eKqnnuNXTil+kS3JEWSrZmv4SL6ljN+TBZnHuO3GPMn+T8lJizEz9tA9ccYoeugtxydyr+sOygYnzAiW8KXCmqRqh8hyUnW7NNrSG/OA/a8iunnQCi3YtikMnvZ0Vh1uylOOT2awPZy9g2MXXbFuGWXoKCqa/kz89iVb3h6MS1QSt1qym53zipm1A54pKSrRnbcn5BpMPk7fw6Rzg6NWp8Bj19pZixE/3m4ubzO2QUTN7Cfw5G/D7bh+JhIQqmHmdFT6/OAJgQuFKnmpL7rSR1EyrfVe35gFVuXFfBSIQKiVAhESokQoVEqJBIhAqJUCERKiRChUSokAgVEqFCIhEqJEKFRKiQCBUSoUIiVEgkQoVEqJBSr/8HbDnFenjYZJAAAAAASUVORK5CYII="
     *         }
     *     ]
     * }
     */
}
