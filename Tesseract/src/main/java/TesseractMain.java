import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/*
PSM Modes:
0 = Orientation and script detection (OSD) only.
1 = Automatic page segmentation with OSD.
2 = Automatic page segmentation, but no OSD, or OCR. (not implemented)
3 = Fully automatic page segmentation, but no OSD. (Default)
4 = Assume a single column of text of variable sizes.
5 = Assume a single uniform block of vertically aligned text.
*6 = Assume a single uniform block of text.
7 = Treat the image as a single text line.
8 = Treat the image as a single word.
9 = Treat the image as a single word in a circle.
10 = Treat the image as a single character.
*11 = Sparse text. Find as much text as possible in no particular order.
12 = Sparse text with OSD.
13 = Raw line. Treat the image as a single text line,
     bypassing hacks that are Tesseract-specific.

OCR Modes:
0 = Original Tesseract only.
1 = Neural nets LSTM only.
2 = Tesseract + LSTM.
3 = Default, based on what is available.
 */

public class TesseractMain {

    public static void main(String[] args) {
        File      file      = new File("Tesseract/src/main/resources/receipt.jpg");
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("Tesseract/src/main/resources/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(6);
//        tesseract.setPageSegMode(11);
        tesseract.setOcrEngineMode(1);
        try {
            String result = tesseract.doOCR(ImageIO.read(file));
            System.out.println(result);
        } catch (TesseractException | IOException e) {
            System.err.println("Could not parse image.");
            e.printStackTrace();
        }
    }

}
