package org.com.identityservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.com.identityservice.constant.AppConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.configure()
//                .filename(".env")
//                .directory(AppConstant.serviceName)
//                .load();
//        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

}
