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
package com.github.sbaudoin.sonar.plugins.ansible.checks;

import org.sonar.api.rule.RuleKey;

public class AnsibleCheckRepository {
    public static final String REPOSITORY_KEY = "ansible";
    public static final String REPOSITORY_NAME = "Ansible Linter";


    /**
     * Hide constructor
     */
    private AnsibleCheckRepository() {
    }


    /**
     * Returns the {@code RuleKey} of the rule identified by its Id as a string
     *
     * @param ruleKey a rule key passed as a string
     * @return a {@code RuleKey} if found or {@code null}
     */
    public static RuleKey getRuleKey(String ruleKey) {
        return RuleKey.of(AnsibleCheckRepository.REPOSITORY_KEY, ruleKey);
    }
}
