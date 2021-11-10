package me.travja.crave.common.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ItemNotFoundException extends NullPointerException {

    public ItemNotFoundException(String s) {
        super(s);
    }
}
