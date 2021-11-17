package me.travja.crave.common.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
@RestController
@RequestMapping
@Component
public @interface CraveController {

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String value();

}
