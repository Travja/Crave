package me.travja.crave.receiptservice;

import me.travja.crave.common.models.Item;
import me.travja.crave.common.repositories.ItemsRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/receipt")
public class ReceiptRestController {

    private       Tesseract       tesseract;
    private final ItemsRepository repo;

    public ReceiptRestController(ItemsRepository repo) {
        this.repo = repo;
        tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(1);
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
    public ReceiptData parseReceipt(@RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
//            RescaleOp op = new RescaleOp(0.8f, 0, null);
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

}
