package me.travja.crave.common.util;

import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.conf.Variables;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class Communication {

    private static LoadBalancerClient loadBalancerClient;
    private static RestTemplate       restTemplate;
    private static Variables          variables;

    private static String auth;

    static {
        loadBalancerClient = AppContext.getBean(LoadBalancerClient.class);
        restTemplate = AppContext.getBean(RestTemplate.class);
        variables = AppContext.getBean(Variables.class);
        auth = "Bearer " + variables.SERVICE_KEY;
    }

    public static <T> T post(String service, String path, String authorization,
                             Object body, Class<? extends T> responseClass) {
        return request(service, path, authorization, HttpMethod.POST, body, responseClass);
    }

    public static <T> T get(String service, String path, String authorization, Class<? extends T> responseClass) {
        return request(service, path, authorization, HttpMethod.POST, null, responseClass);
    }

    public static <T> T request(String service, String path, String authorization,
                                HttpMethod method, Object body, Class<? extends T> responseClass) {
        ServiceInstance serv = loadBalancerClient.choose(service);
        if (serv == null) {
            System.err.println("Couldn't find " + service + "!");
            return null;
        }

        return request(serv.getUri() + path, authorization, method, body, responseClass);
    }

    public static <T> T request(String url, String authorization,
                                HttpMethod method, Object body, Class<? extends T> responseClass) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0");
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
        headers.add(HttpHeaders.AUTHORIZATION,
                authorization == null ? auth : authorization);

        HttpEntity entity;
        if (body != null)
            entity = new HttpEntity(body, headers);
        else
            entity = new HttpEntity(headers);

        ResponseEntity<? extends T> res = restTemplate.exchange(url,
                method, entity, responseClass);

        return res.getBody();
    }

}
