package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.StoreView;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonView({ItemView.class, StoreView.class})
public class Store {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String streetAddress;

    @Getter
    @Setter
    private String city;

    @Getter
    @Setter
    private String state;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "store")
    @JsonView(StoreView.class)
    private List<ItemDetails> items = new ArrayList<>();

}
