package me.travja.crave.common.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Variables {

    @Value("${SERVICE_KEY}")
    public String SERVICE_KEY;

}
