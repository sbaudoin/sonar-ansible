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
package org.sonar.plugins.ansible.checks;

import java.util.Arrays;
import java.util.List;

public class CheckRepository {
    private static final List<Class> CHECK_CLASSES = Arrays.asList(
            // Only checks that do not duplicate existing YAML lint rules
            AlwaysRunCheck.class,
            BecomeUserWithoutBecomeCheck.class,
            CommandHasChangesCheckCheck.class,
            CommandsInsteadOfArgumentsCheck.class,
            CommandsInsteadOfModulesCheck.class,
            EnvVarsInCommandCheck.class,
            GitHasVersionCheck.class,
            MercurialHasRevisionCheck.class,
            OctalPermissionsCheck.class,
            PackageIsNotLatestCheck.class,
            SudoCheck.class,
            TaskHasNameCheck.class,
            TrailingWhitespaceCheck.class,
            UseCommandInsteadOfShellCheck.class,
            UseHandlerRatherThanWhenChangedCheck.class,
            UsingBareVariablesIsDeprecatedCheck.class
    );


    /**
     * Hide constructor
     */
    private CheckRepository() {
    }


    /**
     * Returns all non-syntactical check classes
     *
     * @return all check classes
     */
    public static List<Class> getCheckClasses() {
        return CHECK_CLASSES;
    }
}
