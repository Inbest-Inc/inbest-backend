package com.inbest.backend.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class HikariMonitorController {

    @Autowired
    private HikariDataSource hikariDataSource;

    @GetMapping("/hikari-monitor")
    public Map<String, Object> getHikariConnectionPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("TotalConnections", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
        status.put("ActiveConnections", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
        status.put("IdleConnections", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
        status.put("ThreadsAwaitingConnection", hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        return status;
    }
}
