package me.travja.crave.receiptservice;

//import me.travja.crave.common.models.Item;
//import me.travja.crave.common.repositories.ItemsRepository;

import me.travja.crave.receiptservice.models.TargetItem;
import me.travja.crave.receiptservice.parser.ParserManager;
import me.travja.crave.receiptservice.parser.TargetParser;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/receipt")
public class ReceiptRestController {

    //    private static TessBaseAPI  api;
    //    private final ItemsRepository repo;
    public final RestTemplate restTemplate;


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
    private final ParserManager parserManager;

    //    @GetMapping
//    public List<Item> getItems() {
//        return (List<Item>) repo.findAll();
//    }
//
//    @PostMapping
//    public Item createItem(@RequestBody Item item) {
//        return repo.save(item);
//    }
    private PDFParserConfig    parser   = new PDFParserConfig();
    private TesseractOCRConfig tessConf = new TesseractOCRConfig();
    private ParseContext       ctx      = new ParseContext();
    private Tika               tika;

    public ReceiptRestController(/*ItemsRepository repo, */RestTemplate restTemplate, TargetParser targetParser, ParserManager parserManager) {
//        this.repo = repo;

        this.restTemplate = restTemplate;
        this.parserManager = parserManager;
    }

//    public ReceiptData parse(File f) {
//        PIX img = lept.pixRead("receipt.png");
//        try {
//            //BufferedImage image = ImageIO.read(file.getInputStream());
////            RescaleOp     op    = new RescaleOp(1.2f, 0, null);
////            image = op.filter(image, image);
//            /*Tesseract tesseract;
//            tesseract = new Tesseract();
//            tesseract.setDatapath("tessdata");
//            tesseract.setLanguage("eng");
//            tesseract.setPageSegMode(6);
//            tesseract.setOcrEngineMode(2);
//            String result = tesseract.doOCR(image);*/
//
//            api.SetImage(img);
//            api.SetSourceResolution(70);
//            api.SetPageSegMode(6);
//
//            BytePointer outText = api.GetUTF8Text();
//            String      result  = outText.getString();
////            String result = "walmart\n" +
////                    "CHIMICHANGA 007100701094 F 4.249 \n" +
////                    "CKN WYNGZ 007874207380 F 6.57 \n" +
////                    "GV PSTA SCE 007874200020 F 1.28 \n" +
////                    "LAYS BBQ 002840031040 F 4.30 \n" +
////                    "KEFIR 001707710332 F 2.78 \n" +
////                    "CORNDOGS 007874236164 F 8.97 \n" +
////                    "GV FRT SMIL 007874214463 F 3.98 R";
//
//            System.out.println(result);
//
//            outText.deallocate();
//            BytePointer.free(outText);
//            return new ReceiptData(result, parserManager);
//        } catch (Exception e) {
//            System.err.println("Could not parse image.");
//            e.printStackTrace();
//        } finally {
//            api.Clear();
//            if (img != null) {
//                lept.pixFreeData(img);
//                lept.pixDestroy(img);
//                img.destroy();
//                img.deallocate();
//            }
//            if (f != null)
//                f.delete();
//            System.gc();
//        }
//        return null;
//    }

    @GetMapping
    public List<Object> list() {
        return Collections.emptyList();
    }

    @PostMapping("/parse")
    public ReceiptData parseReceipt(@RequestParam("file") MultipartFile file) {
        return parse(file);
    }

    @PostConstruct
    public void setupOCR() {
        tika = new Tika();
        parser.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.OCR_AND_TEXT_EXTRACTION);
        parser.setOcrDPI(70);
        parser.setDetectAngles(true);
        tessConf.setLanguage("eng");
        tessConf.setPageSegMode("6");
        tessConf.setTessdataPath("tessdata");
        tessConf.setEnableImageProcessing(1);

        ctx.set(Parser.class, new AutoDetectParser(TikaConfig.getDefaultConfig()));
        ctx.set(PDFParserConfig.class, parser);
        ctx.set(TesseractOCRConfig.class, tessConf);
    }

    public ReceiptData parse(MultipartFile f) {
        try {
            InputStream inputStream = f.getInputStream();
            parse(inputStream);
        } catch (IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
        }
        return null;
    }

    public ReceiptData parse(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            tika.getParser().parse(inputStream, new BodyContentHandler(out), new Metadata(), ctx);

            String result = new String(out.toByteArray(), Charset.defaultCharset());

            System.out.println(result);

            return new ReceiptData(result, parserManager);
        } catch (Exception e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
        } finally {
            inputStream.close();
            out.close();
            System.gc();
        }
        return null;
    }

    @PostMapping("/parsestr")
    public ReceiptData parseReceiptString(@RequestParam("file") String base64) {
        String b64        = base64.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(b64);
        File   file       = new File("tmp.png");
        try (ByteArrayInputStream bin = new ByteArrayInputStream(imageBytes);
             FileOutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            out.write(bin.readAllBytes());

            return parse(new FileInputStream(file));
        } catch (IOException e) {
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
