package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.repositories.UPCRepository;
import me.travja.crave.common.views.DetailsView;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.UPCView;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonView({ItemView.class, UPCView.class, DetailsView.class})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long   id;
    private String name  = "No name";
    private String description;
    @JsonIgnore
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, optional = false)
    private UPC    upc;
    @Column(columnDefinition = "TEXT")
    private String image = "/find-image.svg";

    @JsonView(ItemView.class)
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ItemDetails> details = new ArrayList<>();

    public void update(ProductInformation prodInfo) {
        if (getName() == null)
            setName(prodInfo.getName());
            setName(prodInfo.getName());

        if (getUpc() == null)
            setUpc(prodInfo.getUpc());
    }

    @Transient
    @JsonProperty("upc")
    public String getStringUpc() {
        return upc.getUPC();
    }

    public void setUpc(UPC upc) {
        upc.setItem(this);
        this.upc = upc;
    }

    public void setUpc(String upc) {
        UPCRepository repo   = AppContext.getContext().getBean(UPCRepository.class);
        Optional<UPC> target = repo.findById(upc);
        if (target.isEmpty())
            setUpc(new UPC(upc));
        else
            setUpc(target.get());
    }

    @Transient
    public Optional<ItemDetails> getDetails(String store) {
        for (ItemDetails dets : details) {
            if (dets.getStore().getName().equalsIgnoreCase(store))
                return Optional.of(dets);
        }

        return Optional.empty();
    }

    @Transient
    public Optional<ItemDetails> getDetails(Store store) {
        for (ItemDetails dets : details) {
            if (dets.getStore().getName().equalsIgnoreCase(store.getName()))
                return Optional.of(dets);
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }
}
