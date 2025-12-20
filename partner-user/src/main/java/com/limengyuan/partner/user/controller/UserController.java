package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RefreshScope
public class UserController {

    // 模拟用户数据库
    private final Map<Long, User> userDb = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    /**
     * 服务信息接口 - 验证服务是否正常启动
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Map<String, Object> data = Map.of(
                "service", applicationName,
                "port", serverPort,
                "status", "running",
                "timestamp", LocalDateTime.now().toString(),
                "message", "用户服务配置验证成功！");
        return Result.success(data);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("UP");
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        Long id = idGenerator.getAndIncrement();
        user.setUserId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(1);
        userDb.put(id, user);
        return Result.success("创建成功", user);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userDb.get(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> listUsers() {
        return Result.success(new ArrayList<>(userDb.values()));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userDb.get(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        user.setUserId(id);
        user.setCreatedAt(existingUser.getCreatedAt());
        user.setUpdatedAt(LocalDateTime.now());
        userDb.put(id, user);
        return Result.success("更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User removed = userDb.remove(id);
        if (removed == null) {
            return Result.error("用户不存在");
        }
        return Result.success("删除成功", null);
    }
}
