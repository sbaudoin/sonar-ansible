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
package com.github.sbaudoin.sonar.plugins.ansible.extras.rules;

import com.github.sbaudoin.sonar.plugins.ansible.Utils;
import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.ansible.settings.AnsibleSettings;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AnsibleExtraSensor.class})
public class AnsibleExtraSensorTest {
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
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, "src\\test\\resources\\scripts\\ansible-lint4.cmd");
        } else {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, "src/test/resources/scripts/ansible-lint4.sh");
            setShellRights("src/test/resources/scripts/ansible-lint4.sh");
        }

        DummySensorDescriptor descriptor = new DummySensorDescriptor();
        sensor.describe(descriptor);

        assertEquals("Ansible-Lint Sensor with Extra Rules", descriptor.sensorName);
        assertEquals(YamlLanguage.KEY, descriptor.languageKey);

        sensor.execute(context);

        Collection<Issue> issues = context.allIssues();
        assertEquals(3, issues.size());
        assertTrue(issueExists(issues, ruleKey1, playbook1, 3, "An error -p"));
        assertTrue(issueExists(issues, ruleKey2, playbook1, 5, "Another error --nocolor"));
        assertTrue(issueExists(issues, ruleKey2, playbook2, 3, "Another error -r"));
    }

    @Test
    public void testCopyExtraRule() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("copyExtraRule", Path.class, Path.class);
        method.setAccessible(true);

        Path tempDir = Files.createTempDirectory(EXTRA_RULES_TEMP_DIR);

        assertFalse((Boolean)method.invoke(sensor, Paths.get("src/test/resources/extra-rules/extra-rule1.foo"), tempDir));
        Path file = Paths.get("src/test/resources/extra-rules/extra-rule1.cmd");
        assertTrue((Boolean)method.invoke(sensor, file, tempDir));
        assertTrue(tempDir.resolve(file.toFile().getName()).toFile().exists());
        assertEquals(Files.readAllLines(file), Files.readAllLines(tempDir.resolve(file.toFile().getName())));
    }

    @Test
    public void testExtractExtraRulesCannotCreateTempDir() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("extractExtraRules", String.class);
        method.setAccessible(true);

        // Prevent the temporary directory from being created
        mockStatic(Files.class);
        when(Files.createTempDirectory(any(String.class))).thenThrow(new IOException("forbidden"));
        assertNull(method.invoke(sensor, AnsibleExtraSensor.EXTRA_RULES_DIR));
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

    @Test
    public void testExtractExtraRulesExtraRulesExtractionError1() throws Exception {
        testExtractExtraRulesExtraRulesExtractionError(new DirectoryIteratorException(new IOException("forbidden")));
    }

    @Test
    public void testExtractExtraRulesExtraRulesExtractionError2() throws Exception {
        testExtractExtraRulesExtraRulesExtractionError(new URISyntaxException("syntax error", "syntax error"));
    }

    @Test
    public void testExtractExtraRulesExtraRulesExtractionError3() throws Exception {
        testExtractExtraRulesExtraRulesExtractionError(new IOException("forbidden"));
    }

    private void testExtractExtraRulesExtraRulesExtractionError(Exception e) throws Exception {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("extractExtraRules", String.class);
        method.setAccessible(true);

        whenNew(com.github.sbaudoin.sonar.plugins.ansible.util.FileSystem.class).withAnyArguments().thenThrow(e);
        assertNull(method.invoke(sensor, AnsibleExtraSensor.EXTRA_RULES_DIR));
    }

    @Test
    public void testDeleteDirectoryWithError() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("deleteDirectory", Path.class);
        method.setAccessible(true);

        // Create a temporary directory and copy some files
        Path tempDir = Files.createTempDirectory(EXTRA_RULES_TEMP_DIR);
        Path copyFile = tempDir.resolve(Paths.get("extra-rule1.sh"));
        Files.copy(Paths.get("src/test/resources/extra-rules/extra-rule1.sh"), copyFile);

        // First prevent the directory from being removed
        mockStatic(Files.class);
        when(Files.walkFileTree(any(Path.class), any(FileVisitor.class))).thenThrow(new IOException("forbidden"));
        assertFalse((Boolean)method.invoke(sensor, tempDir));
        assertTrue(tempDir.toFile().exists());
        assertTrue(copyFile.toFile().exists());
    }

    @Test
    public void testDeleteDirectory() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Make the method public for test purpose
        Method method = AnsibleExtraSensor.class.getDeclaredMethod("deleteDirectory", Path.class);
        method.setAccessible(true);

        // Create a temporary directory and copy some files
        Path tempDir = Files.createTempDirectory(EXTRA_RULES_TEMP_DIR);
        Path copyFile = tempDir.resolve(Paths.get("extra-rule1.sh"));
        Files.copy(Paths.get("src/test/resources/extra-rules/extra-rule1.sh"), copyFile);

        assertTrue((Boolean)method.invoke(sensor, tempDir));
        assertFalse(tempDir.toFile().exists());
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
