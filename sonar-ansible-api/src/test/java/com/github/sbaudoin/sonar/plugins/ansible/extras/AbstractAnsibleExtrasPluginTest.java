package com.github.sbaudoin.sonar.plugins.ansible.extras;

import junit.framework.TestCase;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

public class AbstractAnsibleExtrasPluginTest extends TestCase {
    public void testExtensionCounts() {
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
        new MyExtrasPlugin().define(context);
        assertEquals(2, context.getExtensions().size());
    }

    private class MyExtrasPlugin extends AbstractAnsibleExtrasPlugin {
    }
}
