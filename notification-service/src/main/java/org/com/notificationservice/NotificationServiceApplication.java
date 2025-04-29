package org.com.notificationservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.com.notificationservice.constant.AppConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .directory(AppConstant.serviceName)
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
