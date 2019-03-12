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

import com.github.sbaudoin.sonar.plugins.ansible.settings.AnsibleSettings;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.github.sbaudoin.sonar.plugins.ansible.checks.AnsibleCheckRepository;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Abstract class for sensors that takes in charge the execution of {@code ansible-lint}. The sensors just have to
 * extend this class, prepare the arguments of {@code ansible-lint} command line in a list and call the
 * {@link #executeCommand(List, List, List)} method in their own {@link #execute(SensorContext)} method. The method
 * {@link #executeCommand(List, List, List)} will automatically deal with the files to be analyzed (the last argument)
 * as well as the formatting options, so child classes should only manage the other command options/arguments.
 * See {@code com.github.sbaudoin.sonar.plugins.ansible.rules.AnsibleSensor} and
 * {@link com.github.sbaudoin.sonar.plugins.ansible.extras.rules.AnsibleExtraSensor} for examples.
 *
 * @see <a href="https://github.com/ansible/ansible-lint">https://github.com/ansible/ansible-lint</a>
 */
public abstract class AbstractAnsibleSensor implements Sensor {
    private static final Logger LOGGER = Loggers.get(AbstractAnsibleSensor.class);

    /**
     * The underlying file system that will give access to the files to be analyzed
     */
    protected final FileSystem fileSystem;

    /**
     * File predicate to filter and select the files to be analyzed with this sensor
     */
    protected final FilePredicate mainFilesPredicate;

    /**
     * All issues found on the analyzed code. This key is the URI to the files where the issues were found and the
     * value is the issue message returned by {@code ansible-lint}
     */
    protected final Map<URI, Set<String>> allIssues = new HashMap<>();

    /**
     * The list of files analyzed by this sensor. As {@code ansible-lint} will not aggregate the issues per file,
     * the result of {@link InputFile#uri()} will be used as the key for {@link #allIssues}.
     */
    protected final Set<InputFile> scannedFiles = new HashSet<>();


    /**
     * Constructor
     *
     * @param fileSystem the underlying file system that will give access to the files to be analyzed
     */
    protected AbstractAnsibleSensor(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.mainFilesPredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(YamlLanguage.KEY));
    }


    /**
     * Executes {@code ansible-lint} with the passed options and saves the issues detected with this tool
     *
     * @param context the execution sensor context (taken from the method {@link #execute(SensorContext)} of the child class)
     * @param extraAnsibleLintArgs the optional list of command arguments for {@code ansible-lint}. May be {@code null}.
     */
    protected void executeWithAnsibleLint(SensorContext context, @Nullable List<String> extraAnsibleLintArgs) {
        LOGGER.debug("Ansible sensor executed with context: " + context);

        // Skip analysis if no rules enabled from this plugin
        if (context.activeRules().findByRepository(AnsibleCheckRepository.REPOSITORY_KEY).isEmpty()) {
            LOGGER.info("No active rules found for this plugin, skipping.");
            return;
        }

        for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
            LOGGER.debug("Analyzing file: " + inputFile.filename());
            scannedFiles.add(inputFile);

            // Build ansible-lint command
            List<String> command = new ArrayList<>();
            command.addAll(Arrays.asList(getAnsibleLintPath(context), "-p", "--nocolor"));
            if (extraAnsibleLintArgs != null) {
                command.addAll(extraAnsibleLintArgs);
            }
            command.add(new File(inputFile.uri()).getAbsolutePath());

            // Execute Ansible Lint and get a parsable output
            List<String> output = new ArrayList<>();
            List<String> error = new ArrayList<>();
            try {
                executeCommand(command, output, error);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (IOException e) {
                return;
            }
            if (!error.isEmpty()) {
                LOGGER.warn("Errors happened during analysis:{}{}",
                        System.getProperty("line.separator"),
                        String.join(System.getProperty("line.separator"), error)
                );
            }

            LOGGER.debug(output.size() + " issue(s) found");
            // Parse output and register all issues: as ansible-lint processes only playbooks but returns issues related to
            // used roles, we need to save all issues first before being able to get role issues and save them
            output.forEach(this::registerIssue);
        }

        // Save all found issues
        saveIssues(context);
    }

    /**
     * Returns the plugin configuration parameter (settings) that defines the path to the command {@code ansible-lint}
     *
     * @param context the execution sensor context (taken from the method {@link #execute(SensorContext)} of the child class)
     * @return the path to the command {@code ansible-lint} or {@literal ansible-lint} if the plugin setting is not set
     * @see AnsibleSettings#ANSIBLE_LINT_PATH_KEY
     */
    protected String getAnsibleLintPath(SensorContext context) {
        Optional<String> path = context.config().get(AnsibleSettings.ANSIBLE_LINT_PATH_KEY);
        return (path.isPresent())?path.get():"ansible-lint";
    }

    /**
     * Executes a system command and writes the standard and error outputs to the passed
     * <code>StringBuilder</code> if not <code>null</code>
     *
     * @param command the command to be executed
     * @param stdOut where the standard output is written to line by line
     * @param errOut where the error output is written to
     * @return the command exit code
     * @throws IOException if an error occurred executing the command. See {@link ProcessBuilder#start()} and {@link Process#waitFor()}
     * @throws InterruptedException if an error occurred executing the command. See {@link ProcessBuilder#start()}
     *                                and {@link Process#waitFor()}
     * @see ProcessBuilder#start()
     * @see Process#waitFor()
     */
    protected int executeCommand(List<String> command, List<String> stdOut, List<String> errOut) throws InterruptedException, IOException {
        assert stdOut != null;
        assert errOut != null;

        LOGGER.debug("Executing command: {}", command);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            // Read standard output
            LineInputReader stdOutputReader = new LineInputReader(p.getInputStream());
            stdOutputReader.start();
            // Wait for thread to be ready
            while (!stdOutputReader.isReady()) {
                Thread.sleep(100);
            }
            // Get error output
            LineInputReader errOutputReader = new LineInputReader(p.getErrorStream());
            errOutputReader.start();
            // Wait for thread to be ready
            while (!errOutputReader.isReady()) {
                Thread.sleep(100);
            }

            int status = p.waitFor();

            // Create standard output lines
            stdOut.addAll(stdOutputReader.getOutput());

            // Write error output if any
            errOut.addAll(errOutputReader.getOutput());

            return status;
        } catch (InterruptedException|IOException e) {
            LOGGER.error("Error executing command: {}", e.getMessage());
            LOGGER.debug("Stack trace:", e);
            throw e;
        }
    }

    /**
     * Adds the passed issue (containing the filename and the issue message) to the list of known issues
     *
     * @param rawIssue an issue as returned by {@code ansible-lint}. The issue must be of the form: "filename:[0-9]+: [E...] ..."
     * @return {@code true} if the issue has been registered, {@code false} if not
     * @see #allIssues
     */
    protected boolean registerIssue(String rawIssue) {
        if (!rawIssue.matches("^.+:[0-9]+: \\[E.+\\] .+$")) {
            LOGGER.warn("Invalid issue syntax, ignoring: " + rawIssue);
            return false;
        }

        String[] tokens = rawIssue.split(":", 2);

        URI fileURI = new File(tokens[0]).toURI();

        if (!allIssues.containsKey(fileURI)) {
            allIssues.put(fileURI, new HashSet<>());
        }
        allIssues.get(fileURI).add(tokens[1]);

        return true;
    }

    /**
     * Saves all found issues in SonarQube. Only the issues of analyzed files will be saved.
     *  Issues to be saved must have been registered first with {@link #registerIssue(String)}.
     *
     * @param context the execution sensor context (taken from the method {@link #execute(SensorContext)} of the child class)
     */
    protected void saveIssues(SensorContext context) {
        for (InputFile inputFile : scannedFiles) {
            LOGGER.debug("Saving issues for {}", inputFile.uri());
            Set<String> issues = allIssues.getOrDefault(inputFile.uri(), new HashSet<>());
            for (String issue : issues) {
                // Saved issues must have been registered first, so we are sure there is an extra :
                String[] tokens = issue.split(":", 2);
                LOGGER.debug("  Saving issue: {}", issue);
                String[] ruleTokens = tokens[1].trim().split("\\] ", 2);
                saveIssue(context, inputFile, Integer.parseInt(tokens[0]), ruleTokens[0].replace("[E", ""), ruleTokens[1]);
            }
        }
    }

    /**
     * Saves the found issues in SonarQube
     *
     * @param context the context
     * @param inputFile the file where the issue was found
     * @param line the line where the issue was found
     * @param ruleId the Id of the rule that raised the issue
     * @param message a message describing the issue
     */
    protected void saveIssue(SensorContext context, InputFile inputFile, int line, String ruleId, String message) {
        RuleKey ruleKey = getRuleKey(context, ruleId);

        // Old rules (ansible-lint < 3.5) had id ANSIBLE... but now it is E... so we may need to add the heading E back
        if (ruleKey == null) {
            ruleKey = getRuleKey(context, "E" + ruleId);
        }

        if (ruleKey == null) {
            LOGGER.debug("Rule " + ruleId + " ignored, not found in repository");
            return;
        }

        NewIssue newIssue = context.newIssue().forRule(ruleKey);
        NewIssueLocation location = newIssue.newLocation()
                .on(inputFile)
                .message(message)
                .at(inputFile.selectLine(line));
        newIssue.at(location).save();
        LOGGER.debug("Issue {} saved for {}", ruleId, inputFile.filename());
    }

    /**
     * Returns the {@code RuleKey} identified as the passed {@code ruleId} or {@code null} if no corresponding active
     * rule has been found
     *
     * @param context the sensor context (that contains the active rules)
     * @param ruleId the rule Id (corresponding to the searched {@code RuleKey})
     * @return the {@code RuleKey} or {@code null} if no active rule has been found
     */
    protected RuleKey getRuleKey(SensorContext context, String ruleId) {
        RuleKey key = AnsibleCheckRepository.getRuleKey(ruleId);
        return (context.activeRules().find(key) != null)?key:null;
    }


    /**
     * Reader class for {@code ansible-lint} output
     */
    private class LineInputReader extends Thread {
        private List<String> output = new ArrayList<>();
        private BufferedReader input;
        private boolean ready = false;


        public LineInputReader(InputStream input) {
            this.input = new BufferedReader(new InputStreamReader(input));
        }

        @Override
        public void run() {
            try {
                String line;
                ready = true;
                while ((line = input.readLine()) != null) {
                    output.add(line);
                    LOGGER.trace("Read from input: {}", line);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot read input stream", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error("Unknown error", e);
                }
            }
        }

        public List<String> getOutput() {
            return output;
        }

        public boolean isReady() {
            return ready;
        }
    }
}
