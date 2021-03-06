/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativeplatform.fixtures.app

import org.gradle.integtests.fixtures.SourceFile

/**
 * A Swift app composed of 3 modules: an executable and 2 libraries. Executable depends on one library only, so that the other library is a transitive dependency of the executable.
 */
class SwiftAppWithLibraries implements AppElement {
    final logger = new SwiftLogger()
    final greeter = new SwiftGreeterUsesLogger()
    final main = new SwiftAlternateMain(greeter)

    SourceElement getLogLibrary() {
        return logger
    }

    @Override
    String getExpectedOutput() {
        return main.expectedOutput
    }

    SourceElement getLibrary() {
        return new SourceElement() {
            @Override
            List<SourceFile> getFiles() {
                return [greeter.sourceFile].collect {
                    sourceFile(it.path, it.name, "import Log\n${it.content}")
                }
            }
        }
    }

    SourceElement getExecutable() {
        return new SourceElement() {
            @Override
            List<SourceFile> getFiles() {
                return [main.sourceFile].collect {
                    sourceFile(it.path, it.name, "import Hello\n${it.content}")
                }
            }
        }
    }
}
