package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.repositories.ManufacturerRepository;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static me.travja.crave.common.views.CraveViews.ItemView;
import static me.travja.crave.common.views.CraveViews.UPCView;

@Getter
@Setter
@Entity
@NoArgsConstructor
@JsonView({ItemView.class, UPCView.class})
public class UPC {

    @Id
    private String       upc;
    @JsonView(UPCView.class)
    @OneToOne(optional = false)
    private Item         item;
    @ManyToOne(cascade = CascadeType.ALL)
    private Manufacturer manufacturer;
    private String       productId;
    private int          checkDigit;

    public UPC(String upc) {
        setUpc(upc);
    }

    @Transient
    @JsonIgnore
    public String getUPC() {
        return manufacturer.getManufacturerId() + productId + calculateCheckDigit();
    }

    @Column(name = "upc")
    public void setUpc(String upc) {
        String                 manId   = upc.substring(0, 6);
        ManufacturerRepository manRepo = (ManufacturerRepository) AppContext.getContext().getBean("manufacturerRepository");
        Optional<Manufacturer> man     = manRepo.findById(manId);
        if (man.isEmpty())
            this.manufacturer = manRepo.save(new Manufacturer(manId, UUID.randomUUID().toString()));
        else
            this.manufacturer = man.get();

        this.productId = upc.substring(6, 11);
        this.checkDigit = Integer.parseInt(upc.substring(11, 12));

        this.upc = upc;
    }

    public int calculateCheckDigit() {
        String upc = this.upc;
        if (upc.startsWith("00")) {
            upc = upc.substring(1);
        } else if (upc.length() == 12) {
            upc = upc.substring(0, upc.length() - 1);
        }

        int odd = 0, even = 0;
        for (int i = 0; i < upc.length(); i++) {
            int num = Integer.parseInt(String.valueOf(upc.charAt(i)));

            if (i % 2 == 0) odd += num;
            else even += num;
        }
        odd *= 3;

        int checkDigit = 10 - (odd + even) % 10;
        if (checkDigit == 10) checkDigit = 0;
        this.checkDigit = checkDigit;

        return checkDigit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UPC upc1 = (UPC) o;
        return upc != null && Objects.equals(upc, upc1.upc);
    }

    @Override
    public String toString() {
        return upc;
    }
}
