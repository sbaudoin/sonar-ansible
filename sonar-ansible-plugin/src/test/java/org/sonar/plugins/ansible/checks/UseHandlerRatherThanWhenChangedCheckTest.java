package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class UseHandlerRatherThanWhenChangedCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new UseHandlerRatherThanWhenChangedCheck());
    }
}
