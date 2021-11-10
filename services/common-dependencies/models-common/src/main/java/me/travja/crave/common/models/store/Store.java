package me.travja.crave.common.models.store;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.item.Sale;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.travja.crave.common.views.CraveViews.*;

@Entity
@Getter
@Setter
@ToString
@JsonView({ItemView.class, StoreView.class, DetailsView.class, SaleView.class, StoreSaleView.class})
public class Store {

    @Id
    @GeneratedValue
    private long   id;
    private String name;
    private String streetAddress;
    private String city;
    private String state;

    @OneToOne(cascade = CascadeType.ALL)
    private Location location = new Location();

    @JsonView(StoreView.class)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "store")
    @ToString.Exclude
    private List<ItemDetails> items = new ArrayList<>();

    @JsonView({StoreView.class, StoreSaleView.class})
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Sale> sales = new ArrayList<>();

    @Column(name = "lat")
    public double getLat() {
        return location.getLat();
    }

    public void setLat(double lat) {
        location.setLat(lat);
    }

    @Column(name = "lon")
    public double getLon() {
        return location.getLon();
    }

    public void setLon(double lon) {
        location.setLon(lon);
    }

    public double getDistance(Location loc) {
        return location.distance(loc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Store store = (Store) o;
        return Objects.equals(id, store.id);
    }
}
