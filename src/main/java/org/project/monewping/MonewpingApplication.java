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

            log.info(".env 파일을 성공적으로 로드했습니다.");

            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                    log.info("환경변수 등록: {} = {}", entry.getKey(), entry.getValue());
                } else {
                    log.info("이미 시스템 프로퍼티에 존재하는 키: {}, 값 무시됨", entry.getKey());
                }
            });
        } catch (DotenvException e) {
            log.warn(".env 파일이 존재하지 않거나 읽을 수 없습니다. 기본 환경변수를 사용합니다.", e);
        }
        SpringApplication.run(MonewpingApplication.class, args);
    }

}
