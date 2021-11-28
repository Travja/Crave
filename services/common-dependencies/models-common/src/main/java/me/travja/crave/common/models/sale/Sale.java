package me.travja.crave.common.models.sale;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.serialization.SaleDeserializer;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Date;

import static me.travja.crave.common.views.CraveViews.*;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonView({SaleView.class, StoreSaleView.class, DetailsView.class, ItemView.class})
@JsonDeserialize(using = SaleDeserializer.class)
public class Sale {

    @Id
    @GeneratedValue
    private long        id;
    @JsonView(SaleView.class)
    @ManyToOne(cascade = CascadeType.ALL)
    private Store       store;
    @ManyToOne(cascade = CascadeType.ALL)
    @JsonView({SaleView.class, StoreSaleView.class})
    private ItemDetails item;
    private double      newPrice;
    private Date        startDate, endDate;

    @Transient
    @Setter(AccessLevel.NONE)
    private long sid; //Store id, just for Json purposes.

    public long getSid() {
        if (store == null) return -1;

        return store.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Sale sale = (Sale) o;
        return id == sale.id;
    }
}
