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
