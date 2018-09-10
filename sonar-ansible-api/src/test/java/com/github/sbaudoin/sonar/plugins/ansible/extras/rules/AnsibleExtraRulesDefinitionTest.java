package com.github.sbaudoin.sonar.plugins.ansible.extras.rules;

import junit.framework.TestCase;

public class AnsibleExtraRulesDefinitionTest extends TestCase {
    public void test() {
        assertEquals("org/sonar/l10n/ansible/rules/ansible-extras", new AnsibleExtraRulesDefinition().getRuleDefinitionPath());
    }
}
