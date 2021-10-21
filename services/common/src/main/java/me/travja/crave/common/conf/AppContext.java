package me.travja.crave.common.conf;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getContext().getBean(clazz);
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

}