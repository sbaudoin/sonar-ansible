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
package com.github.sbaudoin.sonar.plugins.ansible.rules;

public class AnsibleRulesDefinition /*implements RulesDefinition*/extends AbstractAnsibleRulesDefinition {
    public static final String RULES_DEFINITION_FOLDER = "org/sonar/l10n/ansible/rules/ansible";


    @Override
    protected String getRuleDefinitionPath() {
        return RULES_DEFINITION_FOLDER;
    }
}
