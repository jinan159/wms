package com.teamfresh.wms;

import com.teamfresh.wms.infra.config.EmbeddedRedisApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WmsApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(WmsApplication.class);
        app.addListeners(new EmbeddedRedisApplicationListener());
        app.run(args);
    }

}
