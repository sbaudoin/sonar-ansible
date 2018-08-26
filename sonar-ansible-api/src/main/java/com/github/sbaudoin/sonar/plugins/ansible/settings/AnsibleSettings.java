/**
 * Copyright (c) 2018, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.ansible.settings;

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
