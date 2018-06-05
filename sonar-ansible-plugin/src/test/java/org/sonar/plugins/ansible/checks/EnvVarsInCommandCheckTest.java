package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class EnvVarsInCommandCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new EnvVarsInCommandCheck());
    }
}
