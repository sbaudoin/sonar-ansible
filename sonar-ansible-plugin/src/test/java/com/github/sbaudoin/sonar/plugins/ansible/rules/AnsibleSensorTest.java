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
package com.github.sbaudoin.sonar.plugins.ansible.rules;

import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.ansible.settings.AnsibleSettings;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnsibleSensorTest {
    private static final String RULE_ID1 = "AnyCheck1";
    private static final String RULE_ID2 = "AnyCheck2";
    private final RuleKey ruleKey1 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID1);
    private final RuleKey ruleKey2 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID2);
    private AnsibleSensor sensor;
    private SensorContextTester context;


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void test() {
        File moduleBaseDir = new File("src/main/resources");

        try {
            AnsibleSensor sensor = new AnsibleSensor(new DefaultFileSystem(moduleBaseDir));
            DummySensorDescriptor descriptor = new DummySensorDescriptor();
            sensor.describe(descriptor);
            assertEquals("Ansible Lint Sensor", descriptor.sensorName);
            assertEquals(YamlLanguage.KEY, descriptor.languageKey);
        } catch (Exception e) {
            fail("Sensor constructor should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteWithAnsibleLintStdOutput() throws IOException {
        Path baseDir = context.fileSystem().baseDirPath();
        InputFile playbook = TestInputFileBuilder.create("moduleKey", baseDir.resolve("playbooks/playbook1.yml").toString())
                .setModuleBaseDir(Paths.get("."))
                .setContents(new String(Files.readAllBytes(baseDir.resolve("playbooks/playbook1.yml"))))
                .setLanguage(YamlLanguage.KEY)
                .setCharset(StandardCharsets.UTF_8)
                .build();
        context.fileSystem().add(playbook);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, "src\\test\\resources\\scripts\\ansible-lint.cmd");
        } else {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, "src/test/resources/scripts/ansible-lint.sh");
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(Paths.get("src/test/resources/scripts/ansible-lint.sh"), perms);
        }

        sensor.execute(context);
        assertEquals(1, sensor.scannedFiles.size());
        assertTrue(sensor.scannedFiles.contains(playbook));

        Collection<Issue> issues = context.allIssues();
        assertEquals(2, issues.size());
        assertTrue(issueExists(issues, ruleKey1, playbook, 3, "An error -p"));
        assertTrue(issueExists(issues, ruleKey2, playbook, 5, "Another error --nocolor"));
    }

    private boolean issueExists(Collection<Issue> issues, RuleKey ruleKey, InputFile file, int line, String regex) {
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

    @Before
    public void init() throws Exception {
        Path baseDir = Paths.get("src/test/resources");
        context = SensorContextTester.create(baseDir);

        DefaultFileSystem fs = new DefaultFileSystem(baseDir);
        fs.setWorkDir(temporaryFolder.newFolder("temp").toPath());
        context.setFileSystem(fs);

        ActiveRules activeRules = new ActiveRulesBuilder()
                .create(ruleKey1)
                .activate()
                .create(ruleKey2)
                .activate()
                .build();
        context.setActiveRules(activeRules);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        sensor = new AnsibleSensor(fs);
    }


    private class DummySensorDescriptor implements SensorDescriptor {
        private String sensorName;
        private String languageKey;

        @Override
        public SensorDescriptor name(String sensorName) {
            this.sensorName = sensorName;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguage(String languageKey) {
            this.languageKey = languageKey;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguages(String... languageKeys) {
            return this;
        }

        @Override
        public SensorDescriptor onlyOnFileType(InputFile.Type type) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepository(String... repositoryKey) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepositories(String... repositoryKeys) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperty(String... propertyKey) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperties(String... propertyKeys) {
            return this;
        }

        @Override
        public SensorDescriptor global() {
            return this;
        }

        @Override
        public SensorDescriptor onlyWhenConfiguration(Predicate<Configuration> predicate) {
            return this;
        }
    }
}
