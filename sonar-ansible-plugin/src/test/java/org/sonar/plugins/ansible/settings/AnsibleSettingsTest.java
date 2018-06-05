package org.sonar.plugins.ansible.settings;

import junit.framework.TestCase;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;

public class AnsibleSettingsTest extends TestCase {
    public void testGetProperties() {
        List<PropertyDefinition> defs = AnsibleSettings.getProperties();

        assertEquals(1, defs.size());
        assertEquals(AnsibleSettings.ANSIBLE_LINT_PATH_KEY, defs.get(0).key());
        assertEquals(AnsibleSettings.ANSIBLE_LINT_PATH_DEFAULT_VALUE, defs.get(0).defaultValue());
    }
}
