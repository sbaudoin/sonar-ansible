package org.sonar.plugins.ansible.checks;

import junit.framework.TestCase;

public class GitHasVersionCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new GitHasVersionCheck());
    }
}
