<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
SonarQube Plugin for Ansible
============================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/sbaudoin/sonar-ansible.svg?branch=master)](https://travis-ci.org/sbaudoin/sonar-ansible)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-ansible&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-ansible)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-ansible&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-ansible)

A SonarQube plugin to audit and keep under control your Ansible playbooks: apply development standards to your deployment scripts and manage your DevOps technical debt.

## Features
This plugin leverages the [YAML SonarQube plugin](https://github.com/sbaudoin/sonar-yaml/) and adds some additional rules dedicated to Ansible:
* Support for all standard Ansible Lint rules
* Simple extension mechanism to add your own Ansible Lint rules with (very) limited knowledge of Java and Maven

## Installation and execution
### Requirements
* SonarQube 6.7 LTS, 7.9 LTS, 8.0+ (including 8.9 LTS), or 9.0+ (*warning!* tested on 9.0 only)
  with the [SonarQube plugin for YAML](https://github.com/sbaudoin/sonar-yaml/) (the exact required version is detailed in the [release page of the Ansible plugin](/sbaudoin/sonar-ansible/releases)).
  Be aware that the YAML plugin has its own restrictions and compatibility (e.g. the version 1.6.0 and before are not compatible with
  SonarQube 9.1+).
* On the machine that will audit the code:
    * [ansible-lint](https://github.com/ansible/ansible-lint/) version 3.4+ must be installed
    * [Sonar scanner](https://github.com/SonarSource/sonar-scanner-cli) configured to point to your Sonar server

Tested on Linux.

### Installation
1. Download the [YAML](https://github.com/sbaudoin/sonar-yaml/releases) and [Ansible SonarQube](https://github.com/sbaudoin/sonar-ansible/releases) plugins
2. Copy them into the `extensions/plugins` directory of SonarQube and restart SonarQube
3. Log in SonarQube
4. Create a new quality profile for YAML and enable the Ansible rules (search with the tag "ansible")
5. Install Ansible Lint and the Sonar scanner on a Linux machine. If needed, you can set the path to the ansible-lint executable
   in the general settings of SonarQube (see below).
   
### Configuration
The plugin supports different parameters that you can set in at the general or project level:
* Path to the `ansible-lint` executable. If not set, `ansible-lint` is expected to be available in the path (which should be the case for standard ansible-lint installations)
* [As of version 2.2.0](https://github.com/sbaudoin/sonar-ansible/releases/tag/v2.2.0), path to an [ansible-lint configuration file](https://docs.ansible.com/ansible-lint/configuring/configuring.html#configuration-file). If set,
  a `-c` option is passed to ansible-lint with the indicated configuration file path; if not set, no `-c` option is passed and ansible-lint will look at a `.ansible-lint`
  located in the same directory as the `sonar-project.properties` file.

Paths can be absolute or relative. Paths are relative to the root of the project.

### Execution
1. Prior to executing a code audit, you must create a file `sonar-project.properties` that will contain some details about your project (this is a requirement from the Sonar scanner):

    ```INI
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
    ```

    You just have to do that once. Ideally, add this file along with your playbooks in your preferred SCM.
2. Run the Sonar scanner from the playbook directory:

        sonar-scanner

3. Go to SonarQube and check the result

Subsequent scans will just require the last step to be executed. It can easily be integrated into a continuous integration pipeline.

## Severity levels mapping
As of version 3.5, Ansible Lint defines severities. Here is the mapping with SonarQube's severity levels:

| Ansible Lint Level | SonarQube Level |
|--------------------|-----------------|
| `INFO`             | Info            |
| `VERY_LOW`         | Info            |
| `LOW`              | Minor           |
| `MEDIUM`           | Major           |
| `HIGH`             | Critical        |
| `VERY_HIGH`        | Blocker         |

## Standard and extended rules
The default Ansible Lint rules are available by default (but not activated). So far:

| Code                        | `ansible-Lint` version | Description                                                                 |
|-----------------------------|------------------------|-----------------------------------------------------------------------------|
| `ANSIBLE0002`               | 3.4                    | Trailing whitespace                                                         |
| `ANSIBLE0004`               | 3.4                    | Git checkouts must contain explicit version                                 |
| `ANSIBLE0005`               | 3.4                    | Mercurial checkouts must contain explicit revision                          |
| `ANSIBLE0006`               | 3.4                    | Using command rather than module                                            |
| `ANSIBLE0007`               | 3.4                    | Using command rather than an argument to e.g. file                          |
| `ANSIBLE0008`               | 3.4                    | action sudo is deprecated                                                   |
| `ANSIBLE0009`               | 3.4                    | Octal file permissions must contain leading zero                            |
| `ANSIBLE0010`               | 3.4                    | Package installs should not use latest                                      |
| `ANSIBLE0011`               | 3.4                    | All tasks should be named                                                   |
| `ANSIBLE0012`               | 3.4                    | Commands should not change things if nothing needs doing                    |
| `ANSIBLE0013`               | 3.4                    | Use shell only when shell functionality is required                         |
| `ANSIBLE0014`               | 3.4                    | Environment variables don't work as part of command                         |
| `ANSIBLE0015`               | 3.4                    | Using bare variables is deprecated                                          |
| `ANSIBLE0016`               | 3.4                    | Tasks that run when changed should likely be handlers                       |
| `ANSIBLE0017`               | 3.4                    | become_user requires become to work as expected                             |
| `ANSIBLE0018`               | 3.4                    | always_run is deprecated                                                    |
| `ANSIBLE0019`               | 3.4                    | No Jinja2 in when                                                           |
| `E101`                      | 3.5 and 4.*            | Deprecated always_run                                                       |
| `E102`                      | 3.5 and 4.*            | No Jinja2 in when                                                           |
| `E103`                      | 3.5 and 4.*            | Deprecated sudo                                                             |
| `E104`                      | 3.5 and 4.*            | Using bare variables is deprecated                                          |
| `E105`                      | 3.5 and 4.*            | Deprecated module                                                           |
| `E106`                      | 3.5 and 4.*            | Role name does not match `^[a-z][a-z0-9_]+$` pattern                        |
| `E201`                      | 3.5 and 4.*            | Trailing whitespace                                                         |
| `E202`                      | 3.5 and 4.*            | Octal file permissions must contain leading zero                            |
| `E203`                      | 3.5 and 4.*            | Most files should not contain tabs                                          |
| `E204`                      | 3.5 and 4.*            | Lines should be no longer than 120 chars                                    |
| `E205`                      | 3.5 and 4.*            | Use ".yml" or ".yaml" playbook extension                                    |
| `E206`                      | 3.5 and 4.*            | Variables should have spaces before and after: `{{ var_name }}`             |
| `E207`                      | 3.5 and 4.*            | Nested jinja pattern                                                        |
| `E208`                      | 3.5 and 4.*            | File permissions unset or incorrect                                         |
| `E301`                      | 3.5 and 4.*            | Commands should not change things if nothing needs doing                    |
| `E302`                      | 3.5 and 4.*            | Using command rather than an argument to e.g. file                          |
| `E303`                      | 3.5 and 4.*            | Using command rather than module                                            |
| `E304`                      | 3.5 and 4.*            | Environment variables don't work as part of command                         |
| `E305`                      | 3.5 and 4.*            | Use shell only when shell functionality is required                         |
| `E306`                      | 3.5 and 4.*            | Shells that use pipes should set the `pipefail` option                      |
| `E401`                      | 3.5 and 4.*            | Git checkouts must contain explicit version                                 |
| `E402`                      | 3.5 and 4.*            | Mercurial checkouts must contain explicit revision                          |
| `E403`                      | 3.5 and 4.*            | Package installs should not use latest                                      |
| `E404`                      | 3.5 and 4.*            | Doesn't need a relative path in role                                        |
| `E405`                      | 3.5 and 4.*            | Remote package tasks should have a retry                                    |
| `E501`                      | 3.5 and 4.*            | become_user requires become to work as expected                             |
| `E502`                      | 3.5 and 4.*            | All tasks should be named                                                   |
| `E503`                      | 3.5 and 4.*            | Tasks that run when changed should likely be handlers                       |
| `E504`                      | 3.5 and 4.*            | Do not use `local_action`, use `delegate_to: localhost`                     |
| `E505`                      | 3.5 and 4.*            | referenced files must exist                                                 |
| `E601`                      | 3.5 and 4.*            | Don't compare to literal True/False                                         |
| `E602`                      | 3.5 and 4.*            | Don't compare to empty string                                               |
| `E701`                      | 3.5 and 4.*            | `meta/main.yml` should contain relevant info                                |
| `E702`                      | 3.5 and 4.*            | Tags must contain lowercase letters and digits only                         |
| `E703`                      | 3.5 and 4.*            | `meta/main.yml` default values should be changed                            |
| `E704`                      | 3.5 and 4.*            | `meta/main.yml` video_links should be formatted correctly                   |
| `E901`                      | 3.5 and 4.*            | Failed to load or parse file                                                |
| `command-instead-of-module` | 5.*                    | Using command rather than module                                            |
| `command-instead-of-shell`  | 5.*                    | Use shell only when shell functionality is required                         |
| `deprecated-bare-vars`      | 5.*                    | Using bare variables is deprecated                                          |
| `deprecated-command-syntax` | 5.*                    | Using command rather than an argument to e.g. file                          |
| `deprecated-local-action`   | 5.*                    | Do not use 'local_action', use 'delegate_to: localhost'                     |
| `deprecated-module`         | 5.*                    | Deprecated module                                                           |
| `empty-string-compare`      | 5.*                    | Don't compare to empty string                                               |
| `fqcn-builtins`             | 5.*                    | Use FQCN for builtin actions                                                |
| `git-latest`                | 5.*                    | Git checkouts must contain explicit version                                 |
| `hg-latest`                 | 5.*                    | Mercurial checkouts must contain explicit revision                          |
| `ignore-errors`             | 5.*                    | Use failed_when and specify error conditions instead of using ignore_errors |
| `inline-env-var`            | 5.*                    | Command module does not accept setting environment variables inline         |
| `literal-compare`           | 5.*                    | Don't compare to literal True/False                                         |
| `meta-incorrect`            | 5.*                    | `meta/main.yml` default values should be changed                            |
| `meta-no-info`              | 5.*                    | `meta/main.yml` should contain relevant info                                |
| `meta-no-tags`              | 5.*                    | Tags must contain lowercase letters and digits only                         |
| `meta-video-links`          | 5.*                    | `meta/main.yml` video_links should be formatted correctly                   |
| `no-changed-when`           | 5.*                    | Commands should not change things if nothing needs doing                    |
| `no-handler`                | 5.*                    | Tasks that run when changed should likely be handlers                       |
| `no-jinja-nesting`          | 5.*                    | Nested jinja pattern                                                        |
| `no-jinja-when`             | 5.*                    | No Jinja2 in when                                                           |
| `no-log-password`           | 5.*                    | password should not be logged.                                              |
| `no-loop-var-prefix`        | 5.*                    | Role loop_var should use configured prefix.                                 |
| `no-relative-paths`         | 5.*                    | Doesn't need a relative path in role                                        |
| `no-same-owner`             | 5.*                    | Owner should not be kept between different hosts                            |
| `no-tabs`                   | 5.*                    | Most files should not contain tabs                                          |
| `package-latest`            | 5.*                    | Package installs should not use latest                                      |
| `partial-become`            | 5.*                    | become_user requires become to work as expected                             |
| `playbook-extension`        | 5.*                    | Use ".yml" or ".yaml" playbook extension                                    |
| `risky-file-permissions`    | 5.*                    | File permissions unset or incorrect                                         |
| `risky-octal`               | 5.*                    | Octal file permissions must contain leading zero or be a string             |
| `risky-shell-pipe`          | 5.*                    | Shells that use pipes should set the pipefail option                        |
| `role-name`                 | 5.*                    | Role name {0} does not match `^[a-z][a-z0-9_]+$` pattern                    |
| `syntax-check`              | 5.*                    | Ansible syntax check failed                                                 |
| `unnamed-task`              | 5.*                    | All tasks should be named                                                   |
| `var-naming`                | 5.*                    | All variables should be named using only lowercase and underscores          |
| `var-spacing`               | 5.*                    | Variables should have spaces before and after: `{{ var_name }}`             |
| `yaml`                      | 5.*                    | Violations reported by yamllint                                             |


Tags have been added to help identify the rules depending on the `ansible-lint` version installed on your system.

The `Exxx` rules are only available as of version 2.0.0 of this plugin and with `ansible-lint` version 3.5+.
See [the Ansible documentation](https://docs.ansible.com/ansible-lint/rules/default_rules.html).

As this plugin extends the YAML plugin, you can also enable and configure any other YAML rule.

Besides, you can easily add your own Ansible Lint rules: see the [sonar-ansible-extras-plugin](sonar-ansible-extras-plugin) documentation.

## Known issues
### Plugin version 1.x.x not compatible with `ansible-lint` 3.5+
The version 1 of this plugin is not compatible with `ansible-lint` 3.5+. Please use `ansible-lint` version 3.4 with this first release of the plugin.
Be aware that this may also apply to custom rules packaged into the extras plugin.

## License

Copyright 2018-2021 Sylvain BAUDOIN

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)
