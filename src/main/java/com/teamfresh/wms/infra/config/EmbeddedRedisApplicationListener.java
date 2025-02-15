package com.teamfresh.wms.infra.config;

import java.io.IOException;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import redis.embedded.RedisServer;

public class EmbeddedRedisApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private RedisServer redisServer;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        int redisPort = env.getProperty("spring.redis.port", Integer.class, 6379);

        try {
            redisServer = new RedisServer(redisPort);
            redisServer.start();
            System.out.println("Embedded Redis started on port " + redisPort);

            // JVM 종료 시 Redis 종료하도록 shutdown hook 등록
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (redisServer != null) {
                        redisServer.stop();
                        System.out.println("Embedded Redis stopped.");
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start embedded Redis", e);
        }
    }
}
