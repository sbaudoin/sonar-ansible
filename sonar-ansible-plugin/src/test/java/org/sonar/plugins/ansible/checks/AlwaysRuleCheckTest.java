package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class AlwaysRuleCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new AlwaysRuleCheck());
    }
}
