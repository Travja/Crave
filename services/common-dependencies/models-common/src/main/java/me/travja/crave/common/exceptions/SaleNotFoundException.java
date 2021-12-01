package me.travja.crave.common.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SaleNotFoundException extends NullPointerException {

    public SaleNotFoundException(String s) {
        super(s);
    }
}
