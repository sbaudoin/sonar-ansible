package org.sonar.plugins.ansible;

import junit.framework.TestCase;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

public class AnsiblePluginTest extends TestCase {
    public void testExtensionCounts() {
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new AnsiblePlugin().define(context);
        assertEquals(3, context.getExtensions().size());
    }
}
