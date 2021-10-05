package me.travja.crave.receiptservice.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import me.travja.crave.common.models.ItemDetails;
import me.travja.crave.common.views.StoreView;

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
    @JsonView(StoreView.class)
    private List<ItemDetails> items = new ArrayList<>();

}
