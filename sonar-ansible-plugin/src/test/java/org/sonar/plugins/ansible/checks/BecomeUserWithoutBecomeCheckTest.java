package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class BecomeUserWithoutBecomeCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new BecomeUserWithoutBecomeCheck());
    }
}
