package me.travja.crave.common.models;

import lombok.Getter;
import org.springframework.data.domain.Sort;

public enum SortStrategy {
    ALPHABETICAL(Sort.by("name").ascending()),
    LOWEST_FIRST(Sort.by("lowestPrice").ascending()),
    HIGHEST_FIRST(Sort.by("lowestPrice").descending());

    @Getter
    private Sort sort;

    SortStrategy(Sort sort) {
        this.sort = sort;
    }

}
