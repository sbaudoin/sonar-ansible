package org.sonar.plugins.ansible.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.ansible.checks.CheckRepository;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;
import org.sonar.plugins.yaml.languages.YamlLanguage;

import java.util.ArrayList;
import java.util.List;

public class AnsibleRulesDefinition implements RulesDefinition {
    public static final String RULES_DEFINITION_FOLDER = "org/sonar/l10n/ansible/rules/ansible";


    @Override
    public void define(RulesDefinition.Context context) {
        RulesDefinition.NewRepository repository = context.createRepository(CheckRepository.REPOSITORY_KEY, YamlLanguage.KEY).setName(CheckRepository.REPOSITORY_NAME);

        RuleMetadataLoader metadataLoader = new RuleMetadataLoader(RULES_DEFINITION_FOLDER);
        List<Class> allCheckClasses = new ArrayList<>(CheckRepository.getCheckClasses());
        metadataLoader.addRulesByAnnotatedClass(repository, allCheckClasses);

        repository.done();
    }
}
