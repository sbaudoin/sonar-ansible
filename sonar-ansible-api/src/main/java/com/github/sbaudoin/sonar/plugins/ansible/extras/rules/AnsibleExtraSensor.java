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
package com.github.sbaudoin.sonar.plugins.ansible.extras.rules;

import com.github.sbaudoin.sonar.plugins.ansible.rules.AbstractAnsibleSensor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

public final class AnsibleExtraSensor extends AbstractAnsibleSensor {
    public static final String EXTRA_RULES_TEMP_DIR = "ansible-lint-extra-rules";

    private static final Logger LOGGER = Loggers.get(AnsibleExtraSensor.class);


    /**
     * Constructor
     *
     * @param fileSystem the file system on which the sensor will find the files to be analyzed
     */
    public AnsibleExtraSensor(FileSystem fileSystem) {
        super(fileSystem);
    }


    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(YamlLanguage.KEY);
        descriptor.name("Ansible Lint Sensor with Extra Rules");
    }


    @Override
    public void execute(SensorContext context) {
        // Extract extra rules if any
        Path extraRulesDir = handleExtraRules();
        if (extraRulesDir != null) {
            executeWithAnsibleLint(context, Arrays.asList("-r", extraRulesDir.toString()));
            try {
                Files.delete(extraRulesDir);
            } catch (IOException e) {
                LOGGER.warn("Cannot delete temporary directory: " + e.getMessage());
            }
        }
    }


    private Path handleExtraRules() {
        // First copy custom rules in a temporary directory
        Path tempDir;
        try {
            LOGGER.debug("Creating temp dir {}", EXTRA_RULES_TEMP_DIR);
            tempDir = Files.createTempDirectory(EXTRA_RULES_TEMP_DIR, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
            LOGGER.debug("Temp dir create: {}", tempDir.toString());
        } catch (IOException e) {
            // Not a blocker issue: we won't execute the extra rules
            LOGGER.error("Cannot create temporary directory " + EXTRA_RULES_TEMP_DIR, e);
            LOGGER.warn("Extra rules won't be executed");
            return null;
        }

        URL extraRulesDir = getClass().getClassLoader().getResource("extra-rules");
        if (extraRulesDir == null) {
            LOGGER.info("No extra Ansible Lint rules found");
            return null;
        }
        try (com.github.sbaudoin.sonar.plugins.ansible.util.FileSystem fs = new com.github.sbaudoin.sonar.plugins.ansible.util.FileSystem(extraRulesDir.toURI())) {
            LOGGER.debug("Copying extra-rules...");
            fs.readDirectory(extraRulesDir.toURI()).forEach(entry -> copyExtraRule(entry, tempDir));
        } catch (DirectoryIteratorException e) {
            // I/O error encountered during the iteration, the cause is an IOException
            LOGGER.error("Error reading extra-rules directory", e);
            return null;
        } catch (URISyntaxException e) {
            LOGGER.error("Cannot find additional ansible lint rules", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Unknown error", e);
            return null;
        }

        return tempDir;
    }

    private boolean copyExtraRule(Path ruleFile, Path directory) {
        LOGGER.debug("Copying rule script {} to {}", ruleFile.toString(), directory.toString());

        try (OutputStream resStreamOut = new FileOutputStream(directory.resolve(ruleFile.getFileName().toString()).toString())) {
            resStreamOut.write(Files.readAllBytes(ruleFile));
        } catch (IOException e) {
            LOGGER.error("Cannot extract rule " + ruleFile.toString() + " to " + directory.toString(), e);
            return false;
        }

        return true;
    }
}
