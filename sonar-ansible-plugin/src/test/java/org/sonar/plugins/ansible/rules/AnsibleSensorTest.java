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
package org.sonar.plugins.ansible.rules;

import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import org.sonar.plugins.ansible.checks.CheckRepository;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class AnsibleSensorTest {
    private final RuleKey ruleKey = RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, "ANSIBLE0008");
//    private DefaultFileSystem fs;
//    private SensorContextTester context;
//    private AnsibleSensor sensor;


    @Test
    public void test() {
        File moduleBaseDir = new File("src/main/resources");
        SensorContextTester context = SensorContextTester.create(moduleBaseDir);

        ActiveRules activeRules = new ActiveRulesBuilder()
                .create(ruleKey)
                .activate()
                .build();
        CheckFactory checkFactory = new CheckFactory(activeRules);

        try {
            new AnsibleSensor(new DefaultFileSystem(moduleBaseDir), checkFactory);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

//    @Test
//    public void testGetRuleKey() {
//        checkAnsibleLintPath();
//
//        init();
//
//        assertNull(sensor.getRuleKey("foo"));
//        assertNotNull(sensor.getRuleKey("ANSIBLE0008"));
//    }


//    private void checkAnsibleLintPath() {
//        ProcessBuilder pb = new ProcessBuilder();
//        pb.command("ansible-lint", "--version");
//        int exitCode = 1;
//        try {
//            exitCode = pb.start().waitFor();
//        } catch (InterruptedException|IOException e) {
//            // Bad luck, but may happen
//        }
//        assumeTrue(exitCode == 0);
//    }

//    private void init() {
//        File moduleBaseDir = new File("src/test/resources");
//        context = SensorContextTester.create(moduleBaseDir);
//
//        fs = new DefaultFileSystem(moduleBaseDir);
//
//        ActiveRules activeRules = new ActiveRulesBuilder()
//                .create(ruleKey)
//                .activate()
//                .build();
//        CheckFactory checkFactory = new CheckFactory(activeRules);
//
//        sensor = new AnsibleSensor(fs, checkFactory, null);
//    }
}
