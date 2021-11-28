package me.travja.crave.common.models.sale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class BatchSale {

    @Id
    @GeneratedValue
    private long id;

    @OneToMany
    private List<Sale> sales = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String imageData;

}
