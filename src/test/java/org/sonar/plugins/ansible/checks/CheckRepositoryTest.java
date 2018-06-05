package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;
import org.sonar.api.rule.RuleKey;

public class CheckRepositoryTest extends TestCase {
    public void testGetCheckClasses() {
        assertEquals(16, CheckRepository.getCheckClasses().size());
        assertTrue(CheckRepository.getCheckClasses().contains(AlwaysRuleCheck.class));
    }

    public void testGetRuleKey() {
        assertEquals(RuleKey.of(CheckRepository.REPOSITORY_KEY, "foo"), CheckRepository.getRuleKey("foo"));
    }
}
