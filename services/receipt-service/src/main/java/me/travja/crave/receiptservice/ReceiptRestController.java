package me.travja.crave.receiptservice;

import me.travja.crave.common.models.Item;
import me.travja.crave.common.repositories.ItemsRepository;
import me.travja.crave.receiptservice.models.TargetItem;
import me.travja.crave.receiptservice.parser.ParserManager;
import me.travja.crave.receiptservice.parser.TargetParser;
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

    private final ItemsRepository repo;
    public final  RestTemplate    restTemplate;
    private final ParserManager   parserManager;


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


    public ReceiptRestController(ItemsRepository repo, RestTemplate restTemplate, TargetParser targetParser, ParserManager parserManager) {
        this.repo = repo;

        this.restTemplate = restTemplate;
        this.parserManager = parserManager;
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
            BufferedImage image = ImageIO.read(file.getInputStream());
//            RescaleOp     op    = new RescaleOp(1.2f, 0, null);
//            image = op.filter(image, image);
            Tesseract tesseract;
            tesseract = new Tesseract();
            tesseract.setDatapath("tessdata");
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(2);
            String result = tesseract.doOCR(image);
            System.out.println(result);
            return new ReceiptData(result, parserManager);
        } catch (TesseractException | IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/parsestr")
    public ReceiptData parseReceiptString(@RequestParam("file") String base64) {
        try {
            Tesseract tesseract;
            tesseract = new Tesseract();
            tesseract.setDatapath("tessdata");
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(2);
            String        b64        = base64.split(",")[1];
            byte[]        imageBytes = Base64.getDecoder().decode(b64);
            BufferedImage image      = ImageIO.read(new ByteArrayInputStream(imageBytes));
            String        result     = tesseract.doOCR(image);
            System.out.println(result);
            return new ReceiptData(result, parserManager);
        } catch (TesseractException | IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
            return null;
        }
    }


    @GetMapping("/target/{dpci}")
    public TargetItem getTargetInfo(@PathVariable String dpci) {
        return parserManager.getTargetParser().getTargetItem(dpci);
    }

}
