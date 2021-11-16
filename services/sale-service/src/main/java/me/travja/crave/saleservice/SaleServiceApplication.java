package me.travja.crave.saleservice;

import me.travja.crave.common.util.Packages;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = Packages.ENTITIES)
@ComponentScan(basePackages = Packages.COMPONENTS)
@EnableJpaRepositories(basePackages = Packages.REPOSITORIES)
@EnableDiscoveryClient
public class SaleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaleServiceApplication.class, args);
    }

}