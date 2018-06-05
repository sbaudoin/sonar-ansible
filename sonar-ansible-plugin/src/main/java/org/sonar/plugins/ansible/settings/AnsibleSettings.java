package org.sonar.plugins.ansible.settings;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

import static java.util.Arrays.asList;

public class AnsibleSettings {
    public static final String ANSIBLE_LINT_PATH_KEY = "sonar.ansible.ansiblelint.path";
    public static final String ANSIBLE_LINT_PATH_DEFAULT_VALUE = "";


    private AnsibleSettings() {
    }


    public static List<PropertyDefinition> getProperties() {
        return asList(PropertyDefinition.builder(ANSIBLE_LINT_PATH_KEY)
                .name("Path to ansible-lint")
                .description("Path to the ansible-lint executable. Leave it empty if the command is in the system path.")
                .defaultValue(ANSIBLE_LINT_PATH_DEFAULT_VALUE)
                .category("Ansible")
                .onQualifiers(Qualifiers.PROJECT)
                .build());
    }
}
