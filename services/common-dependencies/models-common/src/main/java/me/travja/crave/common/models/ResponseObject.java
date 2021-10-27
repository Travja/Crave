package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Data
public class ResponseObject {

    @JsonIgnore
    @JsonAnyGetter
    private Map<String, Object> data = new HashMap<>();

    @JsonIgnore
    private HttpStatus status = HttpStatus.OK;

    public static ResponseObject success(Object... data) {
        return ResponseObject.of(HttpStatus.OK, "success", true, data);
    }

    public static ResponseObject successConditional(boolean isSuccessful, Object... data) {
        if (isSuccessful)
            return ResponseObject.success(data);
        else
            return ResponseObject.failure(data);
    }

    public static ResponseObject failure(Object... data) {
        return ResponseObject.of(HttpStatus.BAD_REQUEST, "success", false, data);
    }

    public static ResponseObject of(HttpStatus statusCode, String key, Object val) {
        ResponseObject res = new ResponseObject();
        res.setStatus(statusCode);
        res.getData().put(key, val);
        return res;
    }

    /**
     * Parameters should come in pairs (String key, Object value)
     *
     * @param statusCode {@link HttpStatus}
     * @param data
     */
    public static ResponseObject of(HttpStatus statusCode, Object... data) {
        ResponseObject res = new ResponseObject();
        res.setStatus(statusCode);
        for (int i = 0; i + 1 < data.length; i += 2) {
            String key = (String) data[i];
            Object val = data[i + 1];
            res.getData().put(key, val);
        }
        return res;
    }

    public ResponseEntity<Map<String, Object>> build() {
        return new ResponseEntity(getData(), getStatus());
    }

}
