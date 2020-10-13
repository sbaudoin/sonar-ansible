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
package com.github.sbaudoin.sonar.plugins.ansible.rules;

import com.github.sbaudoin.sonar.plugins.ansible.Utils;
import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.ansible.settings.AnsibleSettings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.github.sbaudoin.sonar.plugins.ansible.Utils.issueExists;
import static com.github.sbaudoin.sonar.plugins.ansible.Utils.setShellRights;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

public class AbstractAnsibleSensorTest {
    private static final String RULE_ID1 = "ANSIBLE1";
    private static final String RULE_ID2 = "EAnyCheck1";
    private static final String RULE_ID3 = "EAnyCheck2";
    private final RuleKey ruleKey1 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID1);
    private final RuleKey ruleKey2 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID2);
    private final RuleKey ruleKey3 = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID3);
    private MySensor sensor;
    private SensorContextTester context;


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public LogTester logTester = new LogTester();


    @Test
    public void testExecuteWithAnsibleLintEmptyOutput() throws IOException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY,
                    new File(getClass().getResource("/scripts/ansible-lint1.cmd").getFile()).getAbsolutePath());
        } else {
            String path = new File(getClass().getResource("/scripts/ansible-lint1.sh").getFile()).getAbsolutePath();
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, path);
            setShellRights(path);
        }

        sensor.executeWithAnsibleLint(context, null);
        assertEquals(3, sensor.scannedFiles.size());
        assertTrue(sensor.scannedFiles.contains(playbook1));
        assertTrue(sensor.scannedFiles.contains(playbook2));
        assertTrue(sensor.scannedFiles.contains(playbook3));
        assertEquals(0, context.allIssues().size());
    }

    @Test
    public void testExecuteWithAnsibleLintErrorOutput() throws IOException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY,
                    new File(getClass().getResource("/scripts/ansible-lint2.cmd").getFile()).getAbsolutePath());
        } else {
            String path = new File(getClass().getResource("/scripts/ansible-lint2.sh").getFile()).getAbsolutePath();
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, path);
            setShellRights(path);
        }

        sensor.executeWithAnsibleLint(context, null);
        assertEquals(3, sensor.scannedFiles.size());
        assertTrue(sensor.scannedFiles.contains(playbook1));
        assertTrue(sensor.scannedFiles.contains(playbook2));
        assertTrue(sensor.scannedFiles.contains(playbook3));
        assertEquals(0, context.allIssues().size());

        assertEquals(3, logTester.logs(LoggerLevel.WARN).size());
        logTester.logs(LoggerLevel.WARN).stream().forEach(log -> assertTrue(log.startsWith("Errors happened during analysis:")));
    }

    @Test
    public void testExecuteWithAnsibleLintStdOutput() throws IOException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY,
                    new File(getClass().getResource("/scripts/ansible-lint3.cmd").getFile()).getAbsolutePath());
        } else {
            String path = new File(getClass().getResource("/scripts/ansible-lint3.sh").getFile()).getAbsolutePath();
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, path);
            setShellRights(path);
        }

        sensor.executeWithAnsibleLint(context, Arrays.asList("foo", "bar"));
        assertEquals(3, sensor.scannedFiles.size());
        assertTrue(sensor.scannedFiles.contains(playbook1));
        assertTrue(sensor.scannedFiles.contains(playbook2));
        assertTrue(sensor.scannedFiles.contains(playbook3));

        Collection<Issue> issues = context.allIssues();
        assertEquals(4, issues.size());
        assertTrue(issueExists(issues, ruleKey1, playbook1, 2, "A first error"));
        assertTrue(issueExists(issues, ruleKey2, playbook1, 3, "An error -p"));
        assertTrue(issueExists(issues, ruleKey3, playbook1, 5, "Another error foo"));
        assertTrue(issueExists(issues, ruleKey3, playbook2, 3, "Another error bar"));
    }

    @Test
    public void testExecuteWithAnsibleLintIOException() throws IOException, InterruptedException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        MySensor theSensor = spy(sensor);
        doThrow(new IOException("Boom!")).when(theSensor).executeCommand(any(), any(), any());

        theSensor.executeWithAnsibleLint(context, Arrays.asList("foo", "bar"));
        assertEquals(1, theSensor.scannedFiles.size());

        Collection<Issue> issues = context.allIssues();
        assertEquals(0, issues.size());
    }

    @Test
    public void testExecuteWithAnsibleLintWinthoutConf() throws IOException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        context.fileSystem().add(playbook1);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY,
                    new File(getClass().getResource("/scripts/echo_as_issue.cmd").getFile()).getAbsolutePath());
        } else {
            String path = new File(getClass().getResource("/scripts/echo_as_issue.sh").getFile()).getAbsolutePath();
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, path);
            setShellRights(path);
        }

        sensor.executeWithAnsibleLint(context, null);
        Collection<Issue> issues = context.allIssues();
        assertEquals(1, issues.size());
        assertTrue(issueExists(issues, ruleKey1, playbook1, 2, "-p --nocolor " + Pattern.quote(new File(playbook1.uri()).getAbsolutePath())));
    }

    @Test
    public void testExecuteWithAnsibleLintWinthConf() throws IOException {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        context.fileSystem().add(playbook1);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY,
                    new File(getClass().getResource("/scripts/echo_as_issue.cmd").getFile()).getAbsolutePath());
        } else {
            String path = new File(getClass().getResource("/scripts/echo_as_issue.sh").getFile()).getAbsolutePath();
            context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, path);
            setShellRights(path);
        }

        context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_CONF_PATH_KEY, "/path/to/ansible-lint.conf");

        sensor.executeWithAnsibleLint(context, null);
        Collection<Issue> issues = context.allIssues();
        assertEquals(1, issues.size());
        assertTrue(issueExists(issues, ruleKey1, playbook1, 2, "-p --nocolor -c /path/to/ansible-lint\\.conf " + Pattern.quote(new File(playbook1.uri()).getAbsolutePath())));
    }

    @Test
    public void testGetAnsibleLintPath() {
        assertEquals("ansible-lint", sensor.getAnsibleLintPath(context));
        context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, "/usr/bin/ansible-lint");
        assertEquals("/usr/bin/ansible-lint", sensor.getAnsibleLintPath(context));
    }

    @Test
    public void testGetAnsibleLintConfPath() {
        assertEquals("", sensor.getAnsibleLintConfPath(context));
        context.settings().appendProperty(AnsibleSettings.ANSIBLE_LINT_CONF_PATH_KEY, "/path/to/ansible-lint.conf");
        assertEquals("/path/to/ansible-lint.conf", sensor.getAnsibleLintConfPath(context));
    }

    @Test
    public void testExecuteCommand() {
        ArrayList<String> stdOut = new ArrayList();
        ArrayList<String> stdErr = new ArrayList();

        try {
            sensor.executeCommand(Arrays.asList("invalid-command", "bar"), stdOut, stdErr);
            fail("Invalid/unknown command executed");
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
        }

        try {
            stdOut.clear();
            stdErr.clear();
            ArrayList<String> command = new ArrayList<>();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                command.add(new File(getClass().getResource("/scripts/echo.cmd").getFile()).getAbsolutePath());
            } else {
                String path = new File(getClass().getResource("/scripts/echo.sh").getFile()).getAbsolutePath();
                command.add(path);
                setShellRights(path);
            }
            command.add("foo");
            assertEquals(0, sensor.executeCommand(command, stdOut, stdErr));
            assertEquals(1, stdOut.size());
            assertEquals("foo", stdOut.get(0));
            assertEquals(0, stdErr.size());

            stdOut.clear();
            stdErr.clear();
            assertEquals(0, sensor.executeCommand(Arrays.asList("java", "-version"), stdOut, stdErr));
            assertEquals(0, stdOut.size());
            assertNotEquals(0, stdErr.size());
        } catch (Exception e) {
            fail("Command not executed: " + e.getMessage());
        }
    }

    @Test
    public void testRegisterIssue() {
        assertFalse(sensor.registerIssue("invalid issue"));
        assertFalse(sensor.registerIssue("filename:invalid issue"));
        assertFalse(sensor.registerIssue("filename:a:invalid issue"));
        assertFalse(sensor.registerIssue("filename:12:invalid issue"));
        assertFalse(sensor.registerIssue("filename:12: invalid issue"));
        assertFalse(sensor.registerIssue("filename:12:[xxx]invalid issue"));
        assertFalse(sensor.registerIssue("filename:12: [xxx]invalid issue"));
        assertFalse(sensor.registerIssue("filename:12: [xxx] invalid issue"));
        assertFalse(sensor.registerIssue("filename:12: [Exxx]invalid issue"));
        assertFalse(sensor.registerIssue("filename:12:[Exxx]invalid issue"));
        assertFalse(sensor.registerIssue("filename:12:[Exxx] invalid issue"));
        assertFalse(sensor.registerIssue("filename:12: [Exxx invalid issue"));

        File file1 = new File(context.fileSystem().baseDir().getAbsolutePath(), "path/to/myfile.yml");
        assertTrue(sensor.registerIssue(file1.getPath() + ":2: [Exxx] there is a problem"));
        assertTrue(sensor.registerIssue(file1.getPath() + ":3: [Eyyy] there is another problem"));
        File file2 = new File("/path/to/another/file.yml");
        File file2Absolute = new File(context.fileSystem().baseDir().getAbsolutePath(), file2.getPath());
        assertTrue(sensor.registerIssue(file2.getPath() + ":2: [Exxx] there is a problem"));
        assertEquals(2, sensor.allIssues.size());
        assertEquals(2, sensor.allIssues.get(file1.toURI()).size());
        assertTrue(sensor.allIssues.get(file1.toURI()).contains("2: [Exxx] there is a problem"));
        assertTrue(sensor.allIssues.get(file1.toURI()).contains("3: [Eyyy] there is another problem"));
        assertEquals(1, sensor.allIssues.get(file2Absolute.toURI()).size());
        assertTrue(sensor.allIssues.get(file2Absolute.toURI()).contains("2: [Exxx] there is a problem"));
    }

    @Test
    public void testSaveIssues() throws Exception {
        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        sensor.scannedFiles.add(playbook1);
        sensor.scannedFiles.add(playbook2);
        sensor.scannedFiles.add(playbook3);
        sensor.registerIssue("Bla bla");
        sensor.registerIssue(playbook1.toString() + ":Bla bla");
        sensor.registerIssue(playbook1.toString() + ":2: [EUNKNOWN] Bla bla");
        sensor.registerIssue(playbook1.toString() + ":3: [EAnyCheck1] An error");
        sensor.registerIssue(playbook1.toString() + ":4: [AnyCheck2] Another error");
        sensor.registerIssue(playbook1.toString() + ":5: [EAnyCheck2] Another error");
        sensor.registerIssue(playbook2.toString() + ":3: [EAnyCheck2] Another error");
        sensor.saveIssues(context);

        Collection<Issue> issues = context.allIssues();
        assertEquals(3, issues.size());
        assertTrue(issueExists(issues, ruleKey2, playbook1, 3, "An error"));
        assertTrue(issueExists(issues, ruleKey3, playbook1, 5, "Another error"));
        assertTrue(issueExists(issues, ruleKey3, playbook2, 3, "Another error"));
    }

    @Test
    public void testSaveIssue() throws IOException {
        InputFile playbook = Utils.getInputFile("playbooks/playbook1.yml");

        // Try to save issue for an unknown rule
        logTester.clear();
        sensor.saveIssue(context, playbook, 2, "foo", "An error here");
        assertEquals(1, logTester.logs(LoggerLevel.DEBUG).size());
        assertEquals("Rule foo ignored, not found in repository", logTester.logs(LoggerLevel.DEBUG).get(0));

        // Save issue for a known rule
        logTester.clear();
        sensor.saveIssue(context, playbook, 2, RULE_ID2, "An error here");
        assertEquals(1, context.allIssues().size());
        Issue issue = (Issue)context.allIssues().toArray()[0];
        assertEquals(ruleKey2, issue.ruleKey());
        IssueLocation location = issue.primaryLocation();
        assertEquals(playbook.key(), location.inputComponent().key());
        assertEquals(2, location.textRange().start().line());
        assertEquals("An error here", location.message());
    }

    @Test
    public void testGetRuleKey() throws Exception {
        assertNull(sensor.getRuleKey(context, "foo"));
        assertEquals(RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, RULE_ID2), sensor.getRuleKey(context, RULE_ID2));
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
                .create(ruleKey3)
                .activate()
                .build();
        context.setActiveRules(activeRules);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        sensor = new MySensor(fs);
    }


    private class MySensor extends AbstractAnsibleSensor {
        protected MySensor(FileSystem fileSystem) {
            super(fileSystem);
        }

        @Override
        public void describe(SensorDescriptor descriptor) {
            // Do nothing
        }

        @Override
        public void execute(SensorContext context) {
            executeWithAnsibleLint(context, null);
        }
    }
}
