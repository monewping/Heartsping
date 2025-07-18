package org.project.monewping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MonewpingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonewpingApplication.class, args);
    }

}
