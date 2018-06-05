package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class TrailingWhitespaceCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new TrailingWhitespaceCheck());
    }
}
