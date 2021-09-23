package me.travja.crave.cloudgateway.cloudgateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import reactor.core.publisher.Flux;

public class MyRouteLocator implements RouteLocator {

    @Autowired
    private DiscoveryClient discoveryClient;

    private final RouteLocatorBuilder builder;

    public MyRouteLocator(RouteLocatorBuilder builder) {
        this.builder = builder;
    }

    public Flux<Route> getRoutes() {
        System.out.println("SERVICES");
        RouteLocatorBuilder.Builder build = builder.routes();
        discoveryClient.getServices().forEach(serv -> {
            System.out.println("\t" + serv);
            String path = "/" + serv + "/";
            build.route(serv.toLowerCase(), r -> r.path(path + "**")
                    .filters(f -> f.rewritePath(path + "?(?<remaining>.*)", "/${remaining}"))
                    .uri("lb://" + serv.toUpperCase()));
        });

        build.route("web-server", r -> r.path("/**")
                .uri("lb://WEB-SERVER"));

        return build.build().getRoutes();
    }
}
