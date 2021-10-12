import java.util.List;

public interface ReceiptProcessor {

    List<ProductInformation> parseData(List<String> list);

}
