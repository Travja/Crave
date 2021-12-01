package me.travja.crave.jwt;

import me.travja.crave.common.util.Packages;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {Packages.SERVICES, "me.travja.crave.jwt", "me.travja.crave.common.rabbit"})
@EntityScan(basePackages = Packages.ENTITIES)
@EnableJpaRepositories(basePackages = Packages.REPOSITORIES)
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

}
