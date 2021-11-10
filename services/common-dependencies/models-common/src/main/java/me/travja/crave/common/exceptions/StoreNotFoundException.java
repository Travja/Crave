package me.travja.crave.common.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StoreNotFoundException extends NullPointerException {

    public StoreNotFoundException(String s) {
        super(s);
    }
}
