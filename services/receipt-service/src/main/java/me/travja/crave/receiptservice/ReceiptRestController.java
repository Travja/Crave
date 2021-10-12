package me.travja.crave.receiptservice;

import me.travja.crave.common.models.Item;
import me.travja.crave.common.repositories.ItemsRepository;
import me.travja.crave.receiptservice.models.TargetItem;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/receipt")
public class ReceiptRestController {

    private       Tesseract       tesseract;
    private final ItemsRepository repo;
    public final  RestTemplate    restTemplate;


    /*
Page segmentation modes:
0 = Orientation and script detection (OSD) only.
1 = Automatic page segmentation with OSD.
2 = Automatic page segmentation, but no OSD, or OCR. (not implemented)
3 = Fully automatic page segmentation, but no OSD. (Default)
4 = Assume a single column of text of variable sizes.
5 = Assume a single uniform block of vertically aligned text.
6 = Assume a single uniform block of text.
7 = Treat the image as a single text line.
8 = Treat the image as a single word.
9 = Treat the image as a single word in a circle.
10 = Treat the image as a single character.
11 = Sparse text. Find as much text as possible in no particular order.
12 = Sparse text with OSD.
13 = Raw line. Treat the image as a single text line,
     bypassing hacks that are Tesseract-specific.

Engine mode:
0 = Original Tesseract only.
1 = Neural nets LSTM only.
2 = Tesseract + LSTM.
3 = Default, based on what is available.
     */


    public ReceiptRestController(ItemsRepository repo, RestTemplate restTemplate) {
        this.repo = repo;
        tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(2);
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public List<Item> getItems() {
        return (List<Item>) repo.findAll();
    }

    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return repo.save(item);
    }

    @PostMapping("/parse")
    public ReceiptData parseReceipt(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println(file.getBytes().length + " bytes");
            BufferedImage image = ImageIO.read(file.getInputStream());
//            RescaleOp     op    = new RescaleOp(1.2f, 0, null);
//            image = op.filter(image, image);
            String result = tesseract.doOCR(image);
            System.out.println(result);
            return new ReceiptData(result);
        } catch (TesseractException | IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/parsestr")
    public ReceiptData parseReceiptString(@RequestParam("file") String base64) {
        System.out.println("Hit string based endpoint");
        try {
            String        b64        = base64.split(",")[1];
            byte[]        imageBytes = Base64.getDecoder().decode(b64);
            BufferedImage image      = ImageIO.read(new ByteArrayInputStream(imageBytes));
            String        result     = tesseract.doOCR(image);
            System.out.println(result);
            return new ReceiptData(result);
        } catch (TesseractException | IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
            return null;
        }
    }

    private final String targetVisitorId = "017C7143EA910201809F9312AD49BB2B";
    private final String targetStoreId   = "2641";
    private final String targetKey       = "ff457966e64d5e877fdbad070f276d18ecec4a01";

    @GetMapping("/target/{dpci}")
    public TargetItem getTargetInfo(@PathVariable int dpci) {
        /*
        <head>.*<\/head>
        <script.*?>.*?<\/script>
        <.*?>
        UPC: (\d+)
         */


        String url = new StringBuilder("https://redsky.target.com/redsky_aggregations/v1/web/plp_search_v1"
                + "?key=" + targetKey + "&channel=WEB"
                + "&keyword=" + dpci + "&page=%2Fs%2F" + dpci
                + "&pricing_store_id=" + targetStoreId // Salt Lake
                + "&visitor_id=" + targetVisitorId)
                .toString();

        TargetResponse response = restTemplate.getForObject(url, TargetResponse.class);

        //Should return a single item...
        if (response.getData().getSearch().getProducts().size() >= 1) {
            TargetResponse.TargetData.Product prod = response.getData().getSearch().getProducts().get(0);

            return new TargetItem(prod);
        } else
            return null;
    }

}
