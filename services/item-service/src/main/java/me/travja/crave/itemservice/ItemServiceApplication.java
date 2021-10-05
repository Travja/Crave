package me.travja.crave.itemservice;

import me.travja.crave.common.Packages;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = Packages.ENTITIES)
@ComponentScan(basePackages = Packages.COMPONENTS)
@EnableJpaRepositories(basePackages = Packages.REPOSITORIES)
public class ItemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemServiceApplication.class, args);
    }

}
