package icu.debug.net.wg.core.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-01-28 20:55
 */
class FileHelperTest {

    @Test
    @DisplayName("读取resources下文件测试")
    void testReadResourcesFile() throws IOException {
        assertDoesNotThrow(() -> FileHelper.readResource("mock/read_resources.txt"));
        assertEquals("read_resources_result",FileHelper.readResource("mock/read_resources.txt"));
    }

}