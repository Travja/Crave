package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import me.travja.crave.common.views.DetailsView;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.StoreView;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@JsonView({ItemView.class, StoreView.class, DetailsView.class})
public class ItemDetails {

    @Id
    @GeneratedValue
    private long   id;
    @ManyToOne
    @JsonView(DetailsView.class)
    private Item   item;
    @ManyToOne
    @JsonView({ItemView.class, DetailsView.class})
    private Store  store;
    private double price;

    public ItemDetails(Item item, Store store, double price) {
        setItem(item);
        setStore(store);
        setPrice(price);
    }

    public void update(ProductInformation prodInfo) {
        if (prodInfo.getPrice() > 0) {
            setPrice(prodInfo.getPrice());
        }
        item.update(prodInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ItemDetails that = (ItemDetails) o;
        return Objects.equals(id, that.id);
    }
}
