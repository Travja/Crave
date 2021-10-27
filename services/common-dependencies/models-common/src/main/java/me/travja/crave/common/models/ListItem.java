package me.travja.crave.common.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ListItem {
    @Id
    @GeneratedValue
    public  int     id;
    private String  text    = "";
    private boolean checked = false;
}