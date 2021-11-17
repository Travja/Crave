package me.travja.crave.common.models.item;

import lombok.*;

import javax.persistence.Column;
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
    public  long    id;
    @Column(columnDefinition = "TEXT")
    private String  text    = "";
    private boolean checked = false;
}