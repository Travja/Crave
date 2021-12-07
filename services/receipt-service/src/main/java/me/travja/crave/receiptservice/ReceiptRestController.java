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

package me.travja.crave.receiptservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.receiptservice.models.ReceiptData;
import me.travja.crave.receiptservice.models.TargetItem;
import me.travja.crave.receiptservice.parser.ParserManager;
import me.travja.crave.receiptservice.parser.ReceiptParseException;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@CraveController("/receipt")
public class ReceiptRestController {

    public final  RestTemplate  restTemplate;
    private final ParserManager parserManager;

    private PDFParserConfig    parser   = new PDFParserConfig();
    private TesseractOCRConfig tessConf = new TesseractOCRConfig();
    private ParseContext       ctx      = new ParseContext();
    private Tika               tika;

    @PostMapping("/parse")
    public ResponseObject parseReceipt(@RequestParam("file") MultipartFile file) {
        ReceiptData data = parse(file);
        return ResponseObject.successConditional(data.submit());
    }

    @PostMapping("/parsestr")
    public ResponseObject parseReceiptString(@RequestParam("file") String base64) {
        String b64        = base64.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(b64);
        File   file       = new File("tmp.png");
        try (ByteArrayInputStream bin = new ByteArrayInputStream(imageBytes);
             FileOutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            out.write(bin.readAllBytes());

            ReceiptData data = parse(new FileInputStream(file));

            return ResponseObject.successConditional(data.submit());
        } catch (IOException e) {
            log.info("Could not parse image.");
            e.printStackTrace();
            throw new ReceiptParseException("There was a problem parsing the receipt: " + e.getMessage());
        }
    }

    @GetMapping("/target/{dpci}")
    public TargetItem getTargetInfo(@PathVariable String dpci) {
        return parserManager.getTargetParser().getTargetItem(dpci);
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
            return parse(inputStream);
        } catch (IOException e) {
            log.error("Could not parse image.");
            e.printStackTrace();
            throw new ReceiptParseException("Could not parse image. " + e.getMessage());
        }
    }

    public ReceiptData parse(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            tika.getParser().parse(inputStream, new BodyContentHandler(out), new Metadata(), ctx);

            String result = new String(out.toByteArray(), Charset.defaultCharset());

            if (result.trim().isEmpty())
                throw new ReceiptParseException("Data parsed was empty!");

            return new ReceiptData(result, parserManager);
        } catch (Exception e) {
            log.error("Could not parse image.");
            e.printStackTrace();
            throw new ReceiptParseException("Could not parse image. " + e.getMessage());
        } finally {
            inputStream.close();
            out.close();
            System.gc();
        }
    }

}
