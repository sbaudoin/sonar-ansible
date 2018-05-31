package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class MercurialHasVersionCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new MercurialHasRevisionCheck());
    }
}
