package icu.debug.net.wg.client;

import icu.debug.net.wg.client.command.LightCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.command.annotation.EnableCommand;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-04 1:13
 */
@Slf4j
//@CommandScan
@EnableCommand(LightCommand.class)
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        log.info("wireguard client start ...");
        SpringApplication.run(Application.class, args);
    }
}
