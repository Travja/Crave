package me.travja.crave.common.annotations;

import me.travja.crave.common.util.Packages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootApplication
@EntityScan(basePackages = Packages.ENTITIES)
@ComponentScan(basePackages = Packages.COMPONENTS)
@EnableJpaRepositories(basePackages = Packages.REPOSITORIES)
@EnableDiscoveryClient
@Component
public @interface CraveApplication {
}
