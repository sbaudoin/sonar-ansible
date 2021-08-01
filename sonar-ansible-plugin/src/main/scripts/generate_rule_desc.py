from jinja2 import Environment, BaseLoader, select_autoescape
import re

template_json = '''{
  "title": "{{ title }}",
  "type": "CODE_SMELL",
  "status": "ready",
  "remediation": {
    "func": "Constant\/Issue",
    "constantCost": "5min"
  },
  "tags": [
    "ansible",
    "ansiblelint-5"{% for tag in tags %},
    "{{ tag }}"
    {%- endfor %}
  ],
  "defaultSeverity": "{{ severity }}"
}'''

template_html = '<p>{{ html }}</p>'

severities = {
        '': 'Info',
        'INFO': 'Info',
        'VERY_LOW': 'Info',
        'LOW': 'Minor',
        'MEDIUM': 'Major',
        'HIGH': 'Critical',
        'VERY_HIGH': 'Blocker'
        }

rule_classes = ['AnsibleSyntaxCheckRule',
        'BecomeUserWithoutBecomeRule',
        'CommandHasChangesCheckRule',
        'CommandsInsteadOfArgumentsRule',
        'CommandsInsteadOfModulesRule',
        'ComparisonToEmptyStringRule',
        'ComparisonToLiteralBoolRule',
        'DeprecatedModuleRule',
        'EnvVarsInCommandRule',
        'FQCNBuiltinsRule',
        'GitHasVersionRule',
        'IgnoreErrorsRule',
        'MercurialHasRevisionRule',
        'MetaChangeFromDefaultRule',
        'MetaMainHasInfoRule',
        'MetaTagValidRule',
        'MetaVideoLinksRule',
        'MissingFilePermissionsRule',
        'NestedJinjaRule',
        'NoFormattingInWhenRule',
        'NoLogPasswordsRule',
        'NoSameOwnerRule',
        'NoTabsRule',
        'OctalPermissionsRule',
        'PackageIsNotLatestRule',
        'PlaybookExtension',
        'RoleLoopVarPrefix',
        'RoleNames',
        'RoleRelativePath',
        'ShellWithoutPipefail',
        'TaskHasNameRule',
        'TaskNoLocalAction',
        'UseCommandInsteadOfShellRule',
        'UseHandlerRatherThanWhenChangedRule',
        'UsingBareVariablesIsDeprecatedRule',
        'VariableHasSpacesRule',
        'VariableNamingRule',
        'YamllintRule']


jtemplate = Environment(loader=BaseLoader).from_string(template_json)
htemplate = Environment(loader=BaseLoader, autoescape=select_autoescape()).from_string(template_html)
rhtml_code = re.compile('``([^`]*)``')
rhtml_strong = re.compile('\\*\\*([^*]*)\\*\\*')

for c in rule_classes:
    mod = __import__("ansiblelint.rules.{}".format(c), fromlist=[c])
    rule = getattr(mod, c)
    print("Handling rule {}...".format(rule.id))
    f = open("{}.json".format(rule.id), "w")
    f.write(jtemplate.render(title=rule.shortdesc, tags=rule.tags, severity=severities[rule.severity]))
    f.close()
    f = open("{}.html".format(rule.id), "w")
    f.write(rhtml_strong.sub('<strong>\\1</strong>', rhtml_code.sub('<code>\\1</code>', htemplate.render(html=rule.description))))
    f.close()
