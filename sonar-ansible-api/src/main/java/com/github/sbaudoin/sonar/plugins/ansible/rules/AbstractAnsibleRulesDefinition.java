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

import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.ansible.util.FileSystem;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAnsibleRulesDefinition implements RulesDefinition {
    private static final Logger LOGGER = Loggers.get(AbstractAnsibleRulesDefinition.class);


    @Override
    public void define(RulesDefinition.Context context) {
        RulesDefinition.NewRepository repository = context.createRepository(AnsibleCheckRepository.REPOSITORY_KEY, YamlLanguage.KEY).setName(AnsibleCheckRepository.REPOSITORY_NAME);

        RuleMetadataLoader metadataLoader = new RuleMetadataLoader(getRuleDefinitionPath());

        List<String> keys = new ArrayList<>();
        getRuleKeys().forEach(keys::add);
        metadataLoader.addRulesByRuleKey(repository, keys);

        repository.done();
    }


    protected abstract String getRuleDefinitionPath();


    private List<String> getRuleKeys() {
        List<String> keys = new ArrayList();

        URL definitionDir = getClass().getClassLoader().getResource(getRuleDefinitionPath());
        if (definitionDir == null) {
            LOGGER.info("No Ansible Lint rules found");
            return keys;
        }
        LOGGER.debug("Creating FileSystem for " + definitionDir);
        try (FileSystem fs = new FileSystem(definitionDir.toURI())) {
            LOGGER.debug("Reading rule definition files...");
            fs.readDirectory(definitionDir.toURI()).filter(entry -> entry.toString().endsWith(".json")).forEach(entry -> {
                String key = getRuleKey(entry);
                LOGGER.debug("RuleKey of {} is {}", entry.toString(), key);
                if (htmlDescFileExists(entry)) {
                    keys.add(key);
                } else {
                    LOGGER.warn("Rule {} defined but not described (.html file missing)", key);
                }
            });
        } catch (URISyntaxException e) {
            LOGGER.error("Cannot find additional ansible-lint rules", e);
        } catch (IOException e) {
            LOGGER.error("Unknown error", e);
        }

        return keys;
    }

    private String getRuleKey(Path definitionFile) {
        return definitionFile.getFileName().toString().replace(".json", "");
    }

    private boolean htmlDescFileExists(Path definitionFile) {
        return definitionFile.resolveSibling(getRuleKey(definitionFile) + ".html").toFile().exists();
    }
}
