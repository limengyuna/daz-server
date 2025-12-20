package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务发现测试控制器 - 验证 Nacos 注册中心
 */
@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String applicationName;

    public DiscoveryController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * 获取所有已注册的服务列表
     */
    @GetMapping("/services")
    public Result<List<String>> getServices() {
        return Result.success(discoveryClient.getServices());
    }

    /**
     * 获取当前服务的所有实例
     */
    @GetMapping("/instances")
    public Result<List<ServiceInstance>> getInstances() {
        return Result.success(discoveryClient.getInstances(applicationName));
    }

    /**
     * 获取服务发现详细信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getDiscoveryInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", applicationName);
        info.put("registeredServices", discoveryClient.getServices());

        List<ServiceInstance> instances = discoveryClient.getInstances(applicationName);
        info.put("instanceCount", instances.size());

        if (!instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            info.put("host", instance.getHost());
            info.put("port", instance.getPort());
            info.put("uri", instance.getUri().toString());
            info.put("metadata", instance.getMetadata());
        }

        info.put("nacosStatus", instances.isEmpty() ? "未注册到 Nacos" : "已注册到 Nacos");

        return Result.success(info);
    }
}
