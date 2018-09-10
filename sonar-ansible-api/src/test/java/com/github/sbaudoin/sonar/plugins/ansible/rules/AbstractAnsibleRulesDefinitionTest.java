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

import com.github.sbaudoin.sonar.plugins.ansible.extras.rules.AnsibleExtraRulesDefinition;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AbstractAnsibleRulesDefinitionTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testDefineWithExistingRules() {
        AbstractAnsibleRulesDefinition definition = new MyRulesDefinition("my-rules");
        assertEquals("my-rules", definition.getRuleDefinitionPath());
        testDefineWithExistingRules(definition, false);
    }

    @Test
    public void testExtras() {
        AbstractAnsibleRulesDefinition definition = new AnsibleExtraRulesDefinition();
        assertEquals("org/sonar/l10n/ansible/rules/ansible-extras", definition.getRuleDefinitionPath());
        testDefineWithExistingRules(definition, true);
    }

    private void testDefineWithExistingRules(AbstractAnsibleRulesDefinition rulesDefinition, boolean extra) {
        // Existing rules
        RulesDefinition.Context context = new RulesDefinition.Context();
        rulesDefinition.define(context);
        RulesDefinition.Repository repository = context.repository(AnsibleCheckRepository.REPOSITORY_KEY);

        assertEquals(AnsibleCheckRepository.REPOSITORY_NAME, repository.name());
        assertEquals(YamlLanguage.KEY, repository.language());
        assertEquals(1, repository.rules().size());
        // Expected a warning message for rule2 that has no .html file
        assertTrue(logTester.logs(LoggerLevel.WARN).contains("Rule " + (extra?"extra-":"") + "rule2 defined but not described (.html file missing)"));

        RulesDefinition.Rule aRule = repository.rule((extra?"extra-":"") + "rule1");
        assertNotNull(aRule);
        assertEquals("Any rule", aRule.name());
        // No templates
        assertEquals(0L, repository.rules().stream().filter(RulesDefinition.Rule::template).map(RulesDefinition.Rule::key).count());

        for (RulesDefinition.Rule rule : repository.rules()) {
            for (RulesDefinition.Param param : rule.params()) {
                assertFalse("Description for " + param.key() + " should not be empty", "".equals(param.description()));
            }
        }
    }

    @Test
    public void testDefineWithNonExistingRules() {
        // Existing rules
        MyRulesDefinition rulesDefinition = new MyRulesDefinition("no-rules");
        RulesDefinition.Context context = new RulesDefinition.Context();
        rulesDefinition.define(context);
        RulesDefinition.Repository repository = context.repository(AnsibleCheckRepository.REPOSITORY_KEY);

        assertTrue(repository.rules().isEmpty());
        // Expected an info message for rules not found
        assertTrue(logTester.logs(LoggerLevel.INFO).contains("No Ansible Lint rules found"));
    }

    @Test
    public void testDefineWithKeyError1() {

    }


    private class MyRulesDefinition extends AbstractAnsibleRulesDefinition {
        String path;

        public MyRulesDefinition(String path) {
            this.path = path;
        }


        @Override
        protected String getRuleDefinitionPath() {
            return path;
        }
    }
}
