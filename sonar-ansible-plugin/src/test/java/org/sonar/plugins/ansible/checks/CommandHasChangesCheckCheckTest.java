package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class CommandHasChangesCheckCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new CommandHasChangesCheckCheck());
    }
}
