package me.travja.crave.common.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
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
    private Store store;

    @Getter
    @Setter
    private double price;

}
