package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class OctalPermissionsCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new OctalPermissionsCheck());
    }
}
