/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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

import com.github.sbaudoin.sonar.plugins.ansible.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class AbstractAnsibleSensorNoRuleTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final LogTester logTester = new LogTester();


    @Test
    public void testNoActiveRule() throws IOException {
        SensorContextTester context = Utils.getSensorContext();

        DefaultFileSystem fs = Utils.getFileSystem();
        fs.setWorkDir(temporaryFolder.newFolder("temp").toPath());
        context.setFileSystem(fs);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        InputFile playbook1 = Utils.getInputFile("playbooks/playbook1.yml");
        InputFile playbook2 = Utils.getInputFile("playbooks/playbook2.yml");
        InputFile playbook3 = Utils.getInputFile("playbooks/playbook3.yml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        MySensor sensor = new MySensor(fs);

        sensor.executeWithAnsibleLint(context, null);
        assertEquals(1, logTester.logs(LoggerLevel.INFO).size());
        assertEquals("No active rules found for this plugin, skipping.", logTester.logs(LoggerLevel.INFO).get(0));
        assertEquals(0, context.allIssues().size());
    }


    private static class MySensor extends AbstractAnsibleSensor {
        protected MySensor(FileSystem fileSystem) {
            super(fileSystem);
        }

        @Override
        public void describe(SensorDescriptor descriptor) {
            // Do nothing
        }

        @Override
        public void execute(SensorContext context) {
            executeWithAnsibleLint(context, null);
        }
    }
}
