package me.travja.crave.receiptservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ReceiptServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptServiceApplication.class, args);
    }

}
