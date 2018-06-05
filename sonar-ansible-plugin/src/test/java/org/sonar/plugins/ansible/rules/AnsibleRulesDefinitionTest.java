package org.sonar.plugins.ansible.rules;

import junit.framework.TestCase;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.ansible.checks.CheckRepository;
import org.sonar.plugins.yaml.languages.YamlLanguage;

public class AnsibleRulesDefinitionTest extends TestCase {
    public void testDefine() {
        AnsibleRulesDefinition rulesDefinition = new AnsibleRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        rulesDefinition.define(context);
        RulesDefinition.Repository repository = context.repository(CheckRepository.REPOSITORY_KEY);

        assertEquals(CheckRepository.REPOSITORY_NAME, repository.name());
        assertEquals(YamlLanguage.KEY, repository.language());
        assertEquals(CheckRepository.getCheckClasses().size(), repository.rules().size());

        RulesDefinition.Rule aRule = repository.rule("ANSIBLE0004");
        assertNotNull(aRule);
        assertEquals("Git checkouts must contain explicit version", aRule.name());

        // No templates
        assertEquals(0L, repository.rules().stream().filter(RulesDefinition.Rule::template).map(RulesDefinition.Rule::key).count());

        for (RulesDefinition.Rule rule : repository.rules()) {
            for (RulesDefinition.Param param : rule.params()) {
                assertFalse("Description for " + param.key() + " should not be empty", "".equals(param.description()));
            }
        }
    }
}
