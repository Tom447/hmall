package com.hmall.gateway.toutes;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class RouteConfigLoader {
    private final NacosConfigManager configManager;

    private final RouteDefinitionWriter writer;


    private final static String DATAID = "gateway-routes.json";
    private final static String GROUP = "DEFAULT_GROUP";

    private Set<String> routesIds = new HashSet<>() ;

    @PostConstruct
    public void initRouteConfiguration() throws NacosException {
        //1. 第一次启动时，拉取路由表，并且添加监听器
        String configInfo = configManager.getConfigService().getConfigAndSignListener(DATAID, GROUP, 1000, new Listener() {
            @Override
            public Executor getExecutor() {
                return Executors.newSingleThreadExecutor();
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                //监听到路由变更时更新路由表
                updateRouteConfigInfo(configInfo);
            }
        });
        //写入路由器
        if (configInfo != null) {
            updateRouteConfigInfo(configInfo);
        }
    }

    private void updateRouteConfigInfo(String configInfo) {
        //1. 解析路由信息
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //2. 删除旧的路由
        for (String routeId : routesIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        //3. 判断是否有新的路由
        if (routeDefinitions == null || routeDefinitions.isEmpty()) {
            //无新的路由
            return;
        }
        //4. 更新路由表
        routesIds.clear();
        for (RouteDefinition routeDefinition : routeDefinitions) {
            //4.1 写入路由表
            writer.save(Mono.just(routeDefinition)).subscribe();
            routesIds.add(routeDefinition.getId());
        }
    }
}
