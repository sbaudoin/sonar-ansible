package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class CommandsInsteadOfModulesCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new CommandsInsteadOfModulesCheck());
    }
}
