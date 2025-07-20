package org.project.monewping;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class MonewpingApplication {

    public static void main(String[] args) {
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .load();
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (DotenvException e) {
            log.warn(".env 파일이 존재하지 않거나 읽을 수 없습니다. 기본 환경변수를 사용합니다.");
        }
        SpringApplication.run(MonewpingApplication.class, args);
    }

}
