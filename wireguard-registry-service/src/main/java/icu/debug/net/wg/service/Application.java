package icu.debug.net.wg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-04 1:13
 */
@Slf4j
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        log.info("wireguard service start ...");
        SpringApplication.run(Application.class, args);
        log.info("wireguard service start success");
    }
}
