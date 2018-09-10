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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Sensor class that executes additional ansible-lint rules. The additional ansible-lint rules (the {@code .py} files)
 * must be located in the directory {@code extra-rules}. They must also be described with a pair of HTML and JSON files
 * located in the directory {@code org/sonar/l10n/ansible/rules/ansible-extras}.
 */
public final class AnsibleExtraSensor extends AbstractAnsibleSensor {
    /**
     * Identifier of the temporary directory where the additional rules are extracted
     */
    public static final String EXTRA_RULES_TEMP_DIR = "ansible-lint-extra-rules";

    /**
     * Directory that contains the additional ansible-lint rules
     */
    public static final String EXTRA_RULES_DIR = "extra-rules";

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
        descriptor.name("Ansible-Lint Sensor with Extra Rules");
    }


    @Override
    public void execute(SensorContext context) {
        // Extract extra rules if any
        Path extraRulesDir = extractExtraRules(EXTRA_RULES_DIR);
        if (extraRulesDir != null) {
            executeWithAnsibleLint(context, Arrays.asList("-r", extraRulesDir.toString()));
            deleteDirectory(extraRulesDir);
        }
    }


    /**
     * Extract the files (supposedly containing additional ansible-lint rules) to a temporary directory whose path is
     * returned
     *
     * @param extraRulesDirectory a directory containing files to be "extracted" (copied or unzipped when this class is
     *                            packaged into a JAR file)
     * @return the path where the files where extracted
     */
    private Path extractExtraRules(String extraRulesDirectory) {
        // First copy custom rules in a temporary directory
        Path tempDir;
        try {
            LOGGER.debug("Creating temp dir {}", EXTRA_RULES_TEMP_DIR);
            tempDir = Files.createTempDirectory(EXTRA_RULES_TEMP_DIR);
            LOGGER.debug("Temp dir create: {}", tempDir.toString());
        } catch (IOException e) {
            // Not a blocker issue: we won't execute the extra rules
            LOGGER.error("Cannot create temporary directory " + EXTRA_RULES_TEMP_DIR, e);
            LOGGER.warn("Extra rules won't be executed");
            return null;
        }

        URL extraRulesDir = getClass().getClassLoader().getResource(extraRulesDirectory);
        if (extraRulesDir == null) {
            LOGGER.info("No extra ansible-lint rules found");
            return null;
        }
        try (com.github.sbaudoin.sonar.plugins.ansible.util.FileSystem fs = new com.github.sbaudoin.sonar.plugins.ansible.util.FileSystem(extraRulesDir.toURI())) {
            LOGGER.debug("Copying rules from {}...", extraRulesDirectory);
            fs.readDirectory(extraRulesDir.toURI()).forEach(entry -> copyExtraRule(entry, tempDir));
        } catch (DirectoryIteratorException e) {
            // I/O error encountered during the iteration, the cause is an IOException
            LOGGER.error("Error reading extra-rules directory", e);
            return null;
        } catch (URISyntaxException e) {
            LOGGER.error("Cannot access extra Ansible-lint rule directory", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Unknown error", e);
            return null;
        }

        return tempDir;
    }

    /**
     * Copies or unzips a passed file to a passed directory
     *
     * @param ruleFile a file to be extracted or copied
     * @param directory the directory where to copy or unzip the file
     * @return {@code true} if the file has been successfully extracted, {@code false} if an error occurred and the file
     * could not be extracted
     */
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

    /**
     * Deletes the passed directory
     *
     * @param directory a directory to be deleted, including its content
     * @return {@code true} if the directory could be deleted, {@code false} if not (an error occurred)
     */
    private boolean deleteDirectory(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Cannot delete temporary directory: " + e.getMessage());
            return false;
        }

        return true;
    }
}
