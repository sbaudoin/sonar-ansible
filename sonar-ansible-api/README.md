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
API for Ansible SonarQube Plugin
================================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.sbaudoin/sonar-ansible-api.svg?label=Maven%20Central)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.sbaudoin%22%20AND%20a%3A%22sonar-ansible-api%22)
[![Build Status](https://travis-ci.org/sbaudoin/sonar-ansible.svg?branch=master)](https://travis-ci.org/sbaudoin/sonar-ansible)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-ansible&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-ansible)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-ansible&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-ansible)

Common API used to build the Ansible plugins. Its main purpose is to factorize the call to the `ansible-lint` executable
and ease the definition of rules by simply supplying HTML and JSON files (no need to create Java classes).

For usage examples, please refer to [Ansible plugin](../sonar-ansible-plugin) or [Ansible extras plugin](../sonar-ansible-extras-plugin).

## License

Copyright 2018-2019 Sylvain BAUDOIN

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)