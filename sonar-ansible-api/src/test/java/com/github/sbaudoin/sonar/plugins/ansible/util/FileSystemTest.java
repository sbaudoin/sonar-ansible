/**
 * Copyright (c) 2018, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.ansible.util;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class FileSystemTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testConstructor() throws NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException {
        Field defaultFS = FileSystem.class.getDeclaredField("defaultFileSystem");
        defaultFS.setAccessible(true);
        Field fsField = FileSystem.class.getDeclaredField("fs");
        fsField.setAccessible(true);

        // Test with an existing, non-default provider
        FileSystem fs = new FileSystem(new URI("sftp://localhost/"));
        assertFalse((Boolean)defaultFS.get(fs));
        assertNotEquals(FileSystems.getDefault(), fsField.get(fs));
        fs.close();

        // Test with an invalid URI
        fs = new FileSystem(Paths.get("src").toUri());
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).startsWith("Using default FS because of an error: "));
        assertTrue((Boolean)defaultFS.get(fs));
        assertEquals(FileSystems.getDefault(), fsField.get(fs));

        logTester.clear();
        FileSystem fs2 = new FileSystem(new URI("file:///"));
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).startsWith("FS already exists for URI: "));
        assertEquals(fsField.get(fs), fsField.get(fs2));
    }

    @Test
    public void testReadDirectory() throws URISyntaxException, IOException {
        FileSystem fs = new FileSystem(new URI("file:///"));
        List<Path> content = fs.readDirectory(Paths.get("src", "test", "resources", "my-rules").toUri()).collect(Collectors.toList());
        assertEquals(3, content.size());
        assertEquals("rule1.html", content.get(0).getFileName().toString());
        assertEquals("rule1.json", content.get(1).getFileName().toString());
        assertEquals("rule2.json", content.get(2).getFileName().toString());
    }
}
