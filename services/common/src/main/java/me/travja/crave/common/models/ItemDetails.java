package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.StoreView;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@JsonView({ItemView.class, StoreView.class})
public class ItemDetails {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    @ManyToOne
    private Item item;

    @Getter
    @Setter
    @ManyToOne
    @JsonView(ItemView.class)
    private Store store;

    @Getter
    @Setter
    private double price;

}
