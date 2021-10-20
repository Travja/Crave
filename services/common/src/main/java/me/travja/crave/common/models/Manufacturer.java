package me.travja.crave.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Manufacturer {

    @Id
    @Getter
    @Setter
    private String manufacturerId;

    @Getter
    @Setter
    private String name;

}
