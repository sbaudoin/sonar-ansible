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
* SonarQube 6.6 minimum with the [SonarQube plugin for YAML](https://github.com/sbaudoin/sonar-yaml/) (the exact required version is detailed in the [release page of the Ansible plugin](releases)).
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
   in the general settings of SonarQube.

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
2. Run the Sonar scanner from the playbook directory :

        sonar-scanner

3. Go to SonarQube and check the result

Subsequent scans will just required the last step to be executed. It can easily be integrated into a continuous integration pipeline.

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

| Code          | Description                                                     |
|---------------|-----------------------------------------------------------------|
| `ANSIBLE0002` | Trailing whitespace                                             |
| `ANSIBLE0004` | Git checkouts must contain explicit version                     |
| `ANSIBLE0005` | Mercurial checkouts must contain explicit revision              |
| `ANSIBLE0006` | Using command rather than module                                |
| `ANSIBLE0007` | Using command rather than an argument to e.g. file              |
| `ANSIBLE0008` | action sudo is deprecated                                       |
| `ANSIBLE0009` | Octal file permissions must contain leading zero                |
| `ANSIBLE0010` | Package installs should not use latest                          |
| `ANSIBLE0011` | All tasks should be named                                       |
| `ANSIBLE0012` | Commands should not change things if nothing needs doing        |
| `ANSIBLE0013` | Use shell only when shell functionality is required             |
| `ANSIBLE0014` | Environment variables don't work as part of command             |
| `ANSIBLE0015` | Using bare variables is deprecated                              |
| `ANSIBLE0016` | Tasks that run when changed should likely be handlers           |
| `ANSIBLE0017` | become_user requires become to work as expected                 |
| `ANSIBLE0018` | always_run is deprecated                                        |
| `ANSIBLE0019` | No Jinja2 in when                                               |
| `E101`        | Deprecated always_run                                           |
| `E102`        | No Jinja2 in when                                               |
| `E103`        | Deprecated sudo                                                 |
| `E104`        | Using bare variables is deprecated                              |
| `E105`        | Deprecated module                                               |
| `E201`        | Trailing whitespace                                             |
| `E202`        | Octal file permissions must contain leading zero                |
| `E203`        | Most files should not contain tabs                              |
| `E204`        | Lines should be no longer than 120 chars                        |
| `E205`        | Use ”.yml” or ”.yaml” playbook extension                        |
| `E206`        | Variables should have spaces before and after: `{{ var_name }}` |
| `E301`        | Commands should not change things if nothing needs doing        |
| `E302`        | Using command rather than an argument to e.g. file              |
| `E303`        | Using command rather than module                                |
| `E304`        | Environment variables don’t work as part of command             |
| `E305`        | Use shell only when shell functionality is required             |
| `E306`        | Shells that use pipes should set the `pipefail` option          |
| `E401`        | Git checkouts must contain explicit version                     |
| `E402`        | Mercurial checkouts must contain explicit revision              |
| `E403`        | Package installs should not use latest                          |
| `E404`        | Doesn’t need a relative path in role                            |
| `E405`        | Remote package tasks should have a retry                        |
| `E501`        | become_user requires become to work as expected                 |
| `E502`        | All tasks should be named                                       |
| `E503`        | Tasks that run when changed should likely be handlers           |
| `E504`        | Do not use `local_action`, use `delegate_to: localhost`         |
| `E601`        | Don’t compare to literal True/False                             |
| `E602`        | Don’t compare to empty string                                   |
| `E701`        | `meta/main.yml` should contain relevant info                    |
| `E702`        | Tags must contain lowercase letters and digits only             |
| `E703`        | `meta/main.yml` default values should be changed                |
| `E704`        | `meta/main.yml` video_links should be formatted correctly       |

The `Exxx` rules are only available as of version 2.0.0 of this plugin and with `ansible-lint` version 3.5+.
See [the Ansible documentation](https://docs.ansible.com/ansible-lint/rules/default_rules.html).

As this plugin extends the YAML plugin, you can also enable and configure any other YAML rule.

Besides, you can easily add your own Ansible Lint rules: see the [sonar-ansible-extras-plugin](sonar-ansible-extras-plugin) documentation.

## Known issues
### Plugin version 1.x.x not compatible with `ansible-lint` 3.5+
The version 1 of this plugin is not compatible with `ansible-lint` 3.5+. Please use `ansible-lint` version 3.4 with this first release of the plugin.
Be aware that this may also apply to custom rules packaged into the extras plugin.

## License

Copyright 2018-2019 Sylvain BAUDOIN

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)
