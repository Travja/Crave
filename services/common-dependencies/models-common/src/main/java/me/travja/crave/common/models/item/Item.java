package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.UPCRepository;
import org.hibernate.Hibernate;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static me.travja.crave.common.views.CraveViews.*;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonView({ItemView.class, UPCView.class, DetailsView.class, SaleView.class, StoreSaleView.class})
public class Item {

    private static final String DEF_IMAGE = "/find-image.svg";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long   id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @JsonIgnore
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, optional = false)
    private UPC    upc;
    @Column(columnDefinition = "TEXT")
    private String image = DEF_IMAGE;

    @JsonView(ItemView.class)
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ItemDetails> details = new ArrayList<>();

    @ElementCollection
    private List<String> tags    = new ArrayList();
    @ElementCollection
    private List<String> aliases = new ArrayList();

    @Transient
    private boolean isFavorite = false;

    public Item(long id, String name, String description, UPC upc, String image, List<ItemDetails> details) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.upc = upc;
        this.image = image;
        this.details = details;
    }

    public void update(ProductInformation prodInfo) {
        if (getName() == null)
            setName(prodInfo.getName());

        if (getUpc() == null)
            setUpc(prodInfo.getUpc());

        if (getDescription() == null)
            setDescription(prodInfo.getDescription());

        if (getImage() == DEF_IMAGE)
            setImage(prodInfo.getImage());
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

    public Optional<ItemDetails> getDetails(String store) {
        for (ItemDetails dets : details) {
            if (dets.getStore().getName().equalsIgnoreCase(store))
                return Optional.of(dets);
        }

        return Optional.empty();
    }

    public Optional<ItemDetails> getDetails(Store store) {
        if (store != null)
            return getDetails(store.getName());
        else
            return Optional.empty();
    }

    @PostConstruct
    public void cleanSales() {
        details.forEach(dets -> dets.cleanSales());
    }

    public void addTags(String... tags) {
        Arrays.stream(tags).filter(tag -> !this.tags.contains(tag.toLowerCase()))
                .forEach(tag -> this.tags.add(tag.toLowerCase()));
    }

    public void removeTags(String... tags) {
        this.tags.removeAll(Arrays.stream(tags).map(tag -> tag.toLowerCase()).collect(Collectors.toList()));
    }

    public void addAliases(String... aliases) {
        Arrays.stream(aliases).filter(alias -> !this.aliases.contains(alias.toLowerCase()))
                .forEach(alias -> this.aliases.add(alias.toLowerCase()));
    }

    public void removeAliases(String... aliases) {
        this.aliases.removeAll(Arrays.stream(aliases).map(alias -> alias.toLowerCase()).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }
}
