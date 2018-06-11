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

import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class AnsibleSensorTest {
    @Test
    public void test() {
        File moduleBaseDir = new File("src/main/resources");

        try {
            new AnsibleSensor(new DefaultFileSystem(moduleBaseDir));
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
