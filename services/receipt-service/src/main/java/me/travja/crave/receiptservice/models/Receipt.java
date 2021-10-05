package me.travja.crave.receiptservice.models;

import lombok.Getter;
import lombok.Setter;
import me.travja.crave.common.models.ItemDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL)
    private List<ItemDetails> items = new ArrayList<>();

}
