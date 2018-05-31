package org.sonar.plugins.ansible.rules;

import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.ansible.checks.CheckRepository;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

public class AnsibleSensorTest {
    private final RuleKey ruleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, "ANSIBLE0008");
    private DefaultFileSystem fs;
    private SensorContextTester context;
    private AnsibleSensor sensor;


    @Test
    public void testGetRuleKey() {
        checkAnsibleLintPath();

        init();

        assertNull(sensor.getRuleKey("foo"));
        assertNotNull(sensor.getRuleKey("ANSIBLE0008"));
    }


    private void checkAnsibleLintPath() {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("ansible-lint", "--version");
        int exitCode = 1;
        try {
            exitCode = pb.start().waitFor();
        } catch (InterruptedException|IOException e) {
            // Bad luck, but may happen
        }
        assumeTrue(exitCode == 0);
    }

    private void init() {
        File moduleBaseDir = new File("src/test/resources");
        context = SensorContextTester.create(moduleBaseDir);

        fs = new DefaultFileSystem(moduleBaseDir);

        ActiveRules activeRules = null;
        activeRules = new ActiveRulesBuilder()
                .create(ruleKey)
                .activate()
                .build();
        CheckFactory checkFactory = new CheckFactory(activeRules);

        sensor = new AnsibleSensor(fs, checkFactory, null);
    }
}
