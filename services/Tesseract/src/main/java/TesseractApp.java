import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TesseractApp {

    private static Tesseract tesseract;


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

    static {
        tesseract = new Tesseract();
        tesseract.setDatapath("services/receipt-service/src/main/resources/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(2);
    }

    public static WalmartParser walmartParser = new WalmartParser();

    public static void main(String[] args) {
        File        file = new File("C:/Users/Travja/Downloads/Receipt (4).png");
        ReceiptData data = parseReceipt(file);
    }

    @SneakyThrows
    public static ReceiptData parseReceipt(File file) {
        if (!file.exists()) throw new FileNotFoundException("File doesn't exist!");
        try {
            BufferedImage image = ImageIO.read(file);
            System.out.println("Reading the receipt...\n");
            String result = tesseract.doOCR(image);
            return new ReceiptData(result);
        } catch (TesseractException | IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
            return null;
        }
    }

    public static ReceiptProcessor getParser(ReceiptType type) {
        switch (type) {
            case WALMART:
                return walmartParser;
            default:
                return null;
//            case SMITHS -> {
//            }
//            case TARGET -> {
//            }
//            case UNKNOWN -> {
//            }
        }
    }

    public static class ReceiptData {
        @Getter
        @Setter
        private String data;

        @Getter
        @Setter
        private List<String> list = new ArrayList<>();

        @Getter
        @Setter
        private List<ProductInformation> productData;

        private ReceiptType receiptType;

        public ReceiptData(String data) {
            data = data.replaceAll("\n+", "\n");

            list.addAll(Arrays.stream(data.split("\n"))
                    .filter(s -> s.length() > 1).collect(Collectors.toList()));

            for (String str : list) {
                if (str.toLowerCase().contains("walmart"))
                    receiptType = ReceiptType.WALMART;
                else if (str.toLowerCase().contains("smiths"))
                    receiptType = ReceiptType.SMITHS;
                else if (str.toLowerCase().contains("target"))
                    receiptType = ReceiptType.TARGET;

                if (receiptType != null)
                    break;
            }

            if (receiptType == null)
                receiptType = ReceiptType.UNKNOWN;

            productData = getParser(receiptType).parseData(list);

            this.data = data;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("Receipt Type: ").append(receiptType.toString()).append("\n\n");

            productData.forEach(dat -> sb.append(dat.toString()).append("\n"));
//            list.forEach(str -> sb.append(str).append("\n"));

            return sb.toString();
        }
    }

    public static enum ReceiptType {
        WALMART,
        SMITHS,
        TARGET,
        UNKNOWN
    }

}
