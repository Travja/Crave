package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ResponseObject {

    @JsonIgnore
    @JsonAnyGetter
    private Map<String, Object> data = new HashMap<>();

    public static ResponseObject success(Object... data) {
        return ResponseObject.of("success", true, data);
    }

    public static ResponseObject failure(Object... data) {
        return ResponseObject.of("success", false, data);
    }

    public static ResponseObject of(String key, Object val) {
        ResponseObject res = new ResponseObject();
        res.getData().put(key, val);
        return res;
    }

    /**
     * Parameters should come in pairs (String key, Object value)
     *
     * @param data
     */
    public static ResponseObject of(Object... data) {
        ResponseObject res = new ResponseObject();
        for (int i = 0; i + 1 < data.length; i += 2) {
            String key = (String) data[i];
            Object val = data[i + 1];
            res.getData().put(key, val);
        }
        return res;
    }

}
