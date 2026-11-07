package com.lms;

import com.lms.config.AivenSslInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LmsApplication {

    public static void main(String[] args) {
        AivenSslInitializer.init();
        SpringApplication app = new SpringApplication(LmsApplication.class);
        app.addInitializers(new AivenSslInitializer());
        app.run(args);
    }
}
