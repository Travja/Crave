package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.travja.crave.common.models.store.Store;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import java.util.Objects;

import static me.travja.crave.common.views.CraveViews.*;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@JsonView({ItemView.class, StoreView.class, DetailsView.class, SaleView.class})
public class ItemDetails extends Detail {

    public ItemDetails(Item item, Store store, double price) {
        setItem(item);
        setStore(store);
        setPrice(price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ItemDetails that = (ItemDetails) o;
        return Objects.equals(getId(), that.getId());
    }
}
