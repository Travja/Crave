package me.travja.crave.common.views;

import com.fasterxml.jackson.annotation.JsonView;

public class CraveViews {

    public interface DetailsView extends JsonView {}

    public interface ItemView extends JsonView {}

    public interface StoreView extends JsonView {}

    public interface UPCView extends JsonView {}

}
