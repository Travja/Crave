package me.travja.crave.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
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
    @Setter
    @OneToOne (mappedBy = "item")
    private UPC upc;

    @Getter
    @Setter
    @Column(columnDefinition = "TEXT")
    private String image = "https://www.seriouseats.com/thmb/3JoYWz3_PajrDhL57P9eQrpg-xE=/735x0" +
            "/__opt__aboutcom__coeus__resources__content_migration__serious_eats__seriouseats.com__recipes__images__2015__07__20150702-sous-vide-hamburger-anova-primary-bf5eefff4505446f9cbf33f5f2d9b2e6.jpg";

}
