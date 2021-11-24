package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.UPCRepository;
import org.hibernate.Hibernate;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static me.travja.crave.common.views.CraveViews.*;

@Slf4j
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
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<ItemDetails> details = new HashSet<>();

    @ElementCollection
    private List<String> tags    = new ArrayList();
    @ElementCollection
    private List<String> aliases = new ArrayList();

    @Transient
    private boolean isFavorite = false;

    private double lowestPrice = 0;

    public Item(long id, String name, String description, UPC upc, String image, Set<ItemDetails> details) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.upc = upc;
        this.image = image;
        this.details = details;
    }

    public void update(ProductInformation prodInfo, List<String> authorities) {
        boolean isAdmin = authorities.contains("ADMIN");
        log.info("Admin? " + isAdmin);
        if (prodInfo.getName() != null && (getName() == null || isAdmin)) {
            setName(prodInfo.getName());
            log.info("Set name to " + prodInfo.getName());
        }

        if (prodInfo.getUpc() != null && (getUpc() == null || isAdmin))
            setUpc(prodInfo.getUpc());

        if (prodInfo.getDescription() != null && (getDescription() == null || isAdmin)) {
            setDescription(prodInfo.getDescription());
            log.info("Set description");
        }

        if (prodInfo.getImage() != null && prodInfo.getImage().equals(DEF_IMAGE) && (getImage().equals(DEF_IMAGE) || isAdmin)) {
            setImage(prodInfo.getImage());
            log.info("Set image");
        }

        setLowestPrice(getLowestPrice());
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

    @Column(name = "lowest_price")
    public double getLowestPrice() {
        double price = Double.MAX_VALUE;
        for (ItemDetails det : details) {
//            if (det instanceof PendingDetails) continue;

            if (det.getLowestPrice() < price)
                price = det.getLowestPrice();
        }
        return price;
    }

    @PostConstruct
    public void cleanSales() {
        details.forEach(dets -> dets.cleanSales());
        setLowestPrice(getLowestPrice());
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
