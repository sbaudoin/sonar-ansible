package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class PackageIsNotLatestCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new PackageIsNotLatestCheck());
    }
}
