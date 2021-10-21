package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import me.travja.crave.common.views.DetailsView;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.StoreView;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@JsonView({ItemView.class, StoreView.class, DetailsView.class})
public class Store {

    @Id
    @GeneratedValue
    private long   id;
    private String name;
    private String streetAddress;
    private String city;
    private String state;

    @JsonView(StoreView.class)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "store")
    @ToString.Exclude
    private List<ItemDetails> items = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Store store = (Store) o;
        return Objects.equals(id, store.id);
    }
}
