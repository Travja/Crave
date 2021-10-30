package me.travja.crave.receiptservice.models;

import lombok.Data;
import me.travja.crave.common.models.item.ProductInformation;
import me.travja.crave.receiptservice.parser.TargetResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class TargetItem extends ProductInformation {

    private static final Pattern upcPat = Pattern.compile("UPC: (\\d+)", Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);

    public TargetItem(TargetResponse.TargetData.Product product) {
        super(product.getItem().getProductDescription().getTitle(),
                getTargetUPC(product.getTcin()),
                product.getItem().getEnrichment().getPrimaryImageUrl(),
                product.getItem().getProductDescription().getDescription(),
                product.getRetailPrice());
    }

    private static String getTargetUPC(String tcin) {
        String upc = null;
        try {
            URL               url = new URL("https://www.target.com/p/-/A-" + tcin);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIES.01; Windows NT)");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.connect();

            int status = con.getResponseCode();

            if (status == 404)
                return upc;

            BufferedReader in      = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String         inputLine;
            StringBuffer   content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();


            /*
            <head>.*<\/head>
            <script.*?>.*?<\/script>
            <.*?>
            UPC: (\d+)
            */

            String result = content.toString().replaceAll("<head>.*<\\/head>", "")
                    .replaceAll("<script.*?>.*?<\\/script>", "")
                    .replaceAll("<.*?>", "")
                    .replace("\n", " ");

            Matcher mat = upcPat.matcher(result);
            if (mat.find())
                upc = mat.group(1);
            else
                System.out.println("Could not find a UPC for this item.");
        } catch (IOException e) {
            System.err.println("UPC not found. Problem requesting resources from Target.");
            e.printStackTrace();
        }

        return upc;
    }

}
