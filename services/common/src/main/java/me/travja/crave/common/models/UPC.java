package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.travja.crave.common.AppContext;
import me.travja.crave.common.repositories.ManufacturerRepository;
import me.travja.crave.common.views.ItemView;
import me.travja.crave.common.views.UPCView;

import javax.persistence.*;
import java.util.Optional;
import java.util.UUID;

@Entity
@NoArgsConstructor
@JsonView({ItemView.class, UPCView.class})
public class UPC {

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    private Manufacturer manufacturer;
    @Getter
    @Setter
    private String       productId;
    @Getter
    @Setter
    private int          checkDigit;
    @Getter
    @Setter
    @OneToOne(optional = false)
    @JsonView(UPCView.class)
    private Item         item;
    @Id
    private String       upc;

    public UPC(String upc) {
        setUPC(upc);
    }

    @JsonIgnore
    public String getUPC() {
        return manufacturer.getManufacturerId() + productId + calculateCheckDigit();
    }

    @Column(name = "upc")
    public void setUPC(String upc) {
        String                 manId   = upc.substring(0, 6);
        ManufacturerRepository manRepo = (ManufacturerRepository) AppContext.getContext().getBean("manufacturerRepository");
        Optional<Manufacturer> man     = manRepo.findById(manId);
        if (man.isEmpty())
            this.manufacturer = manRepo.save(new Manufacturer(manId, UUID.randomUUID().toString()));
        else
            this.manufacturer = man.get();

        //5410116545419
        this.productId = upc.substring(6, 11);
        this.checkDigit = Integer.parseInt(upc.substring(11, 12));

        System.out.println(manufacturer.getManufacturerId() + " -- " + productId + " -- " + calculateCheckDigit());
        this.upc = upc;
    }

    public int calculateCheckDigit() {
        return checkDigit;
    }

}
