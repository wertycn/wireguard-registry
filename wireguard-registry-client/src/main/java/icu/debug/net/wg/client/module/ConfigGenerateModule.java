package icu.debug.net.wg.client.module;

import icu.debug.net.wg.core.WireGuardConfigGenerator;
import icu.debug.net.wg.core.helper.FileHelper;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-04 1:53
 */
@Slf4j
@Component
public class ConfigGenerateModule {

    private final WireGuardNetProperties defaultProperties;

    public ConfigGenerateModule(WireGuardNetProperties defaultProperties) {
        this.defaultProperties = defaultProperties;
        log.info("default properties {}", this.defaultProperties);
    }

    public List<Path> generateFiles(String structFilePath, String outputPath) {
        // 指定路径读取文件
        String content = readFile(structFilePath);
        // 加载为JSON
        WireGuardNetworkStruct struct = WireGuardNetworkStruct.ofJson(content);
        // 生成配置内容
        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(struct, defaultProperties);
        Map<String, WireGuardIniConfig> configMap = generator.buildWireGuardIniConfigs();
        // 生成配置文件
        initOutputDir(outputPath);
        List<Path> result = new ArrayList<>();
        configMap.forEach((k, v) -> result.add(createConfigFile(outputPath, k, v.toIniString())));
        // 返回生成的配置文件路径列表
        return result;

    }

    private static Path createConfigFile(String outputPath, String name, String config) {
        //
        Path path = Paths.get(outputPath, name + ".conf");
        //Path file = Files.c(path);
        try {
            Files.write(path, config.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    private static void initOutputDir(String outputPath) {
        Path outputPathDir = Paths.get(outputPath);
        boolean exists = Files.exists(outputPathDir);
        if (exists) {
            return;
        }

        try {
            Files.createDirectory(outputPathDir);
        } catch (IOException e) {
            throw new IllegalArgumentException("create output dir [" + outputPath + "] error:" + e.getMessage(), e);
        }

        Assert.isTrue(Files.isDirectory(outputPathDir), "output path it must dir");
    }

    private static String readFile(String structFilePath) {
        try {
            return FileHelper.read(structFilePath);
        } catch (IOException e) {
            throw new IllegalArgumentException("file: [" + Paths.get(structFilePath) + "] struct file load failed:" + e.getMessage(), e);
        }
    }
}
