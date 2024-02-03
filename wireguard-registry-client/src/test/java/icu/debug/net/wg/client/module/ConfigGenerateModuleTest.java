package icu.debug.net.wg.client.module;

import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-04 2:35
 */
@DisplayName("配置生成模块测试")
class ConfigGenerateModuleTest {

    @Test
    @DisplayName("读取本地配置文件")
    void testReadFile() throws IOException {
        WireGuardNetProperties properties = new WireGuardNetProperties();
        properties.setAddress("10.201.1.1");
        properties.setNetmask("255.255.255.0");
        ConfigGenerateModule module = new ConfigGenerateModule(properties);
        Path thatPath = Paths.get("./").toAbsolutePath();
        List<Path> paths = module.generateFiles("src/test/resources/wireguard-network-example.json", "target/test-generate");
        paths.forEach(path -> Assertions.assertThat(path).isNotEmptyFile());
    }

}