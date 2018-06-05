package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class SudoCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new SudoCheck());
    }
}
