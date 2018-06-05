package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class UsingBareVariablesIsDeprecatedCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new UsingBareVariablesIsDeprecatedCheck());
    }
}
