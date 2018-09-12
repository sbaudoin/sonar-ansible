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
package com.github.sbaudoin.sonar.plugins.ansible;

import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static final String MODULE_KEY = "moduleKey";

    public static final Path BASE_DIR = Paths.get("src", "test", "resources");


    private Utils() {
    }


    public static InputFile getInputFile(String relativePath) throws IOException {
        return TestInputFileBuilder.create(MODULE_KEY, BASE_DIR.resolve(relativePath).toString())
                .setModuleBaseDir(Paths.get("."))
                .setContents(new String(Files.readAllBytes(BASE_DIR.resolve(relativePath))))
                .setLanguage(YamlLanguage.KEY)
                .setCharset(StandardCharsets.UTF_8)
                .build();
    }

    public static SensorContextTester getSensorContext() {
        return SensorContextTester.create(BASE_DIR);
    }

    public static DefaultFileSystem getFileSystem() {
        return new DefaultFileSystem(BASE_DIR);
    }

    public static boolean issueExists(Collection<Issue> issues, RuleKey ruleKey, InputFile file, int line, String regex) {
        // Brut force...
        for (Issue issue : issues) {
            IssueLocation location = issue.primaryLocation();
            if (issue.ruleKey().equals(ruleKey) &&
                    file.key().equals(location.inputComponent().key()) &&
                    line == location.textRange().start().line() &&
                    location.message().matches(regex)
            ) {
                return true;
            }
        }
        return false;
    }

    public static void setShellRights(String path) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(Paths.get(path), perms);
    }
}
