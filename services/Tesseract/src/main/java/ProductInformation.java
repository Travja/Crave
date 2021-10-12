import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ProductInformation {

    @Getter
    @Setter
    public String name, upc, price;

    @Override
    public String toString() {
        return "ProductInformation{" +
                "name='" + name + '\'' +
                ", upc='" + upc + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
