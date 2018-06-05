package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class TaskHasNameCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new TaskHasNameCheck());
    }
}
