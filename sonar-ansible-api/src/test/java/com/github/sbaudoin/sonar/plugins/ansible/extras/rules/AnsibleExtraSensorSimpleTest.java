/**
 * Copyright (c) 2018-2019, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.ansible.extras.rules;

import com.github.sbaudoin.sonar.plugins.ansible.Utils;
import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.ansible.settings.AnsibleSettings;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collection;
import java.util.function.Predicate;

import static com.github.sbaudoin.sonar.plugins.ansible.Utils.issueExists;
import static com.github.sbaudoin.sonar.plugins.ansible.Utils.setShellRights;
import static com.github.sbaudoin.sonar.plugins.ansible.extras.rules.AnsibleExtraSensor.EXTRA_RULES_TEMP_DIR;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

public class AnsibleExtraSensorSimpleTest {
    private static final String RULE_ID1 = "AnyCheck1";
    private static final String RULE_ID2 = "AnyCheck2";
    private final RuleKey ruleKey1 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID1);
    private final RuleKey ruleKey2 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID2);
    private AnsibleExtraSensor sensor;
    private SensorContextTester context;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void testExecute() throws IOException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY,
                    new File(getClass().getResource("/scripts/ansible-lint4.cmd").getFile()).getAbsolutePath());
        } else {
            String path = new File(getClass().getResource("/scripts/ansible-lint4.sh").getFile()).getAbsolutePath();
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, path);
            setShellRights(path);
        }

        DummySensorDescriptor descriptor = new DummySensorDescriptor();
        sensor.describe(descriptor);

        assertEquals("Ansible-Lint Sensor with Extra Rules", descriptor.sensorName);
        assertEquals(YamlLanguage.KEY, descriptor.languageKey);

        sensor.execute(context);

        Collection<Issue> issues = context.allIssues();
        assertEquals(4, issues.size());
        assertTrue(issueExists(issues, ruleKey1, playbook1, 3, "An error -p"));
        assertTrue(issueExists(issues, ruleKey1, playbook1, 4, null));
        assertTrue(issueExists(issues, ruleKey2, playbook1, 5, "Another error --nocolor"));
        assertTrue(issueExists(issues, ruleKey2, playbook2, 3, "Another error -q"));
    }

    @Test
    public void testCopyExtraRule() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException, URISyntaxException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("copyExtraRule", Path.class, Path.class);
        method.setAccessible(true);

        Path tempDir = Files.createTempDirectory(EXTRA_RULES_TEMP_DIR);

        assertFalse((Boolean)method.invoke(sensor, Paths.get("src/test/resources/extra-rules/extra-rule1.foo"), tempDir));
        Path file = Paths.get(getClass().getResource("/extra-rules/extra-rule1.cmd").toURI());
        assertTrue((Boolean)method.invoke(sensor, file, tempDir));
        assertTrue(tempDir.resolve(file.toFile().getName()).toFile().exists());
        assertEquals(Files.readAllLines(file), Files.readAllLines(tempDir.resolve(file.toFile().getName())));
    }

    @Test
    public void testExtractExtraRulesExtraRulesDirNotFound() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("extractExtraRules", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(sensor, "foo"));
    }

    @Test
    public void testExtractExtraRulesExtraRules() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("extractExtraRules", String.class);
        method.setAccessible(true);

        Path tempDir = (Path)method.invoke(sensor, AnsibleExtraSensor.EXTRA_RULES_DIR);
        assertEquals(4, tempDir.toFile().list().length);
        assertTrue(tempDir.resolve("extra-rule1.cmd").toFile().exists());
        assertTrue(tempDir.resolve("extra-rule1.sh").toFile().exists());
        assertTrue(tempDir.resolve("extra-rule2.cmd").toFile().exists());
        assertTrue(tempDir.resolve("extra-rule2.sh").toFile().exists());
    }

    @Before
    public void init() throws Exception {
        context = Utils.getSensorContext();

        DefaultFileSystem fs = Utils.getFileSystem();
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

        sensor = new AnsibleExtraSensor(context.fileSystem());
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
