package me.travja.crave.itemservice.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
public class UPC {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Getter
    @Setter
    @ManyToOne
    private Manufacturer manufacturer;

    @Getter
    @Setter
    private long productId;

    @Getter
    @Setter
    private long checkDigit;

    @Getter
    @Setter
    @OneToOne
    private Item item;

    @Column(name = "upc")
    public long getQualifiedUPC() {
        return Long.parseLong(manufacturer.getManufacturerId() + String.valueOf(productId) + checkDigit);
    }

}
