package com.cloudnative.config.sample;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class ConfigApplication {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private Environment environment;

    @GetMapping("/")
    public Object index() {
        Map<String, Object> map = new TreeMap<>();
        discoveryClient.getServices().forEach(serviceId -> map.put(serviceId, discoveryClient.getInstances(serviceId)));
        return map;
    }

    @GetMapping(value = {"/services", "/services/{serviceId}"})
    public List<String> services(@PathVariable(required = false) String serviceId) {
        List<String> services = discoveryClient.getServices();
        if (StringUtils.isNoneEmpty(serviceId)) {
            return services.stream().filter(item -> item.equals(serviceId)).collect(Collectors.toList());
        }
        return services;
    }

    @GetMapping(value = {"/service_instances", "/service_instances/{serviceId}"})
    public List<ServiceInstance> serviceInstances(@PathVariable(required = false) String serviceId) {
        if (StringUtils.isNoneEmpty(serviceId)) {
            return discoveryClient.getInstances(serviceId);
        }
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        discoveryClient.getServices().forEach(item -> {
            serviceInstances.addAll(discoveryClient.getInstances(item));
        });
        return serviceInstances;
    }

    @GetMapping("/info")
    public String info() {
        return String.format("%s:%s:%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), environment.getProperty("spring.profiles.active"), environment.getProperty("HOSTNAME"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }

}
