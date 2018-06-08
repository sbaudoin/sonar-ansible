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
package org.sonar.plugins.ansible.rules;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import org.sonar.plugins.ansible.checks.CheckRepository;
import org.sonar.plugins.yaml.languages.YamlLanguage;

public class AnsibleSensor extends AbstractAnsibleSensor {
    /**
     * Constructor
     *
     * @param fileSystem the file system on which the sensor will find the files to be analyzed
     * @param checkFactory check factory used to get the checks to execute against the files
     * @param fileLinesContextFactory factory used to report measures
     */
    public AnsibleSensor(FileSystem fileSystem, CheckFactory checkFactory/*, FileLinesContextFactory fileLinesContextFactory*/) {
        super(fileSystem/*,
                checkFactory.create(AnsibleCheckRepository.REPOSITORY_KEY).addAnnotatedChecks((Iterable<?>) CheckRepository.getCheckClasses()),
                fileLinesContextFactory*/);
    }


    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(YamlLanguage.KEY);
        descriptor.name("Ansible Lint Sensor");
    }

    @Override
    public void execute(SensorContext context) {
        executeWithAnsibleLint(context, null);
    }
}
