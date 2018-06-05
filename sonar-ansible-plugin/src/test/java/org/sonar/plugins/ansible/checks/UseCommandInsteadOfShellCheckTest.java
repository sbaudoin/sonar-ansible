package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class UseCommandInsteadOfShellCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new UseCommandInsteadOfShellCheck());
    }
}
