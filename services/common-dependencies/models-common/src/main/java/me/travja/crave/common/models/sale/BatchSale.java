package me.travja.crave.common.models.sale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.travja.crave.common.AsyncCaller;
import me.travja.crave.common.conf.AppContext;

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

    private boolean approved = false;

    public void approve() {
        approved = true;
        sales.forEach(sale -> sale.approve(false));

        AsyncCaller async = AppContext.getBean(AsyncCaller.class);
        async.handleSaleApproved(sales);
    }

}
