SonarQube Plugin for Ansible
============================

A SonarQube plugin to audit and keep under control your Ansible playbooks: apply development standards to your deployment scripts.

[![Build Status](https://travis-ci.org/sbaudoin/sonar-ansible.svg?branch=master)](https://travis-ci.org/sbaudoin/sonar-ansible)

## Features
This plugin leverages the [YAML SonarQube plugin](https://github.com/sbaudoin/sonar-yaml/) and adds some additional rules dedicated to Ansible:
* Support for all standard Ansible Lint rules
* Simple extension mechanism to add your own Ansible Lint rules with (very) limited knowledge of Java and Maven

## Installation and execution
### Requirements
* SonarQube 6.7 minimum with the [SonarQube plugin for YAML](https://github.com/sbaudoin/sonar-yaml/)
* On the machine that will audit the code:
    * [ansible-lint](https://github.com/willthames/ansible-lint/) must be installed
    * [Sonar scanner](https://github.com/SonarSource/sonar-scanner-cli) configured to point to your Sonar server

Tested on Linux.

### Installation
1. Download the YAML and Ansible SonarQube plugins
2. Install them into SonarQube
3. Log in SonarQube
4. Create a new quality profile for YAML if needed and enable the Ansible rules (search with the tag "ansible")
5. Install Ansible Lint and the Sonar scanner on a Linux machine

### Execution
1. Prior to executing a code audit, you must create a file `sonar-project.properties` that will contain some details about your project (this is a requirement from the Sonar scanner):

        # must be unique in a given SonarQube instance
        sonar.projectKey=com.mycompany:my-playbook
        # this is the name and version displayed in the SonarQube UI. Was mandatory prior to SonarQube 6.1.
        sonar.projectName=A Name
        sonar.projectVersion=1.0-SNAPSHOT
        
        # Path is relative to the sonar-project.properties file. Replace "\" by "/" on Windows.
        # This property is optional if sonar.modules is set.
        sonar.sources=.
        
        # Encoding of the source code. Default is default system encoding
        #sonar.sourceEncoding=UTF-8

    You just have to do that once. Ideally, add this file along with your playbooks in your preferred SCM.
2. Run the Sonar scanner from the playbook directory :

        sonar-scanner

3. Go to SonarQube and check the result

Subsequent scans will just required the last step to be executed. It can easily be integrated into a continuous integration pipeline.

## Standard and extended rules
The default Ansible Lint rules are available by default (but not activated). So far:
* AlwaysRunRule
* BecomeUserWithoutBecomeRule
* CommandHasChangesCheckRule
* CommandsInsteadOfArgumentsRule
* CommandsInsteadOfModulesRule
* EnvVarsInCommandRule
* GitHasVersionRule
* MercurialHasRevisionRule
* OctalPermissionsRule
* PackageIsNotLatestRule
* SudoRule
* TaskHasNameRule
* TrailingWhitespaceRule
* UseCommandInsteadOfShellRule
* UseHandlerRatherThanWhenChangedRule
* UsingBareVariablesIsDeprecatedRule

As this plugin extends the YAML plugin, you can also enable and configure any other YAML rule.

Besides, you can easily add your own Ansible Lint rules: see the [sonar-ansible-extras-plugin](sonar-ansible-extras-plugin) documentation.

## License

Copyright 2018 Sylvain BAUDOIN

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)