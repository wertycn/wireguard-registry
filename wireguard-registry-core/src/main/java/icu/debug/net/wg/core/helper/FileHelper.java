package icu.debug.net.wg.core.helper;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * 文件操作助手类
 *
 * @author hanjinxiang@debug.icu
 * @date 2024-01-28 20:43
 */
@UtilityClass
public class FileHelper {

    public static String readResource(String path) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        return FileUtils.readFileToString(classPathResource.getFile(), "UTF-8");
    }

}
