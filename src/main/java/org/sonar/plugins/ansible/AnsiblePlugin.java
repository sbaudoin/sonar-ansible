package org.sonar.plugins.ansible;

import org.sonar.api.Plugin;
import org.sonar.plugins.ansible.rules.AnsibleRulesDefinition;
import org.sonar.plugins.ansible.rules.AnsibleSensor;
import org.sonar.plugins.ansible.settings.AnsibleSettings;

public class AnsiblePlugin implements Plugin {
    @Override
    public void define(Context context) {
        // Add plugin settings (file extensions, etc.)
        context.addExtensions(AnsibleSettings.getProperties());

        // Extends YAML rules
        context.addExtensions(AnsibleRulesDefinition.class, AnsibleSensor.class);
    }
}
