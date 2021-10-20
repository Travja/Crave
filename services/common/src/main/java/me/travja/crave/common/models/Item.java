package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.travja.crave.common.AppContext;
import me.travja.crave.common.repositories.UPCRepository;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.UPCView;

import javax.persistence.*;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonView({ItemView.class, UPCView.class})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private long id;

    @Getter
    @Setter
    private String name = "No name";

    @Getter
    @Setter
    private String description;

    @Getter
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, optional = false)
    @JsonView(ItemView.class)
    private UPC upc;

    @Getter
    @Setter
    @Column(columnDefinition = "TEXT")
    private String image = "https://www.seriouseats.com/thmb/3JoYWz3_PajrDhL57P9eQrpg-xE=/735x0" +
            "/__opt__aboutcom__coeus__resources__content_migration__serious_eats__seriouseats.com__recipes__images__2015__07__20150702-sous-vide-hamburger-anova-primary-bf5eefff4505446f9cbf33f5f2d9b2e6.jpg";

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

}
