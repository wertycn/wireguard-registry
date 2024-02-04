package icu.debug.net.wg.client.command;

import icu.debug.net.wg.client.module.ConfigGenerateModule;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;
import java.util.List;

/**
 * 轻量模式相关命令
 *
 * @author hanjinxiang@debug.icu
 * @date 2024-02-04 0:44
 */
@Command(interactionMode = InteractionMode.NONINTERACTIVE)
public class LightCommand {

    private final ConfigGenerateModule module;

    public LightCommand(ConfigGenerateModule module) {
        this.module = module;
    }

    @Command(command = "generate", alias = "gen", description = "基于组网规划配置生成组网配置文件", interactionMode = InteractionMode.NONINTERACTIVE)
    public String helloWorld(
            @ShellOption(value = "struct_file_path", help = "网络规划JSON配置") String structFile,
            @ShellOption(value = "output_path", defaultValue = "./", help = "生成结果存储路径") String output
    ) {
        //new WireGuardConfigGenerator(new);
        List<Path> paths = module.generateFiles(structFile, output);


        return String.format("""
                wireguard struct file: [%s] , output path: [%s]
                generate config files:
                %s
                """, structFile, output, String.join("\n", paths.stream().map(Path::toString).toList()));
    }
}
