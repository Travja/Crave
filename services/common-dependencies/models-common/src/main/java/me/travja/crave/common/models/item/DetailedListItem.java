package me.travja.crave.common.models.item;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetailedListItem extends ListItem {
    private ItemDetails lowestDetails;

    public DetailedListItem(long id, String text, boolean checked, ItemDetails lowestDetails) {
        super(id, text, checked);
        this.lowestDetails = lowestDetails;
    }
}
