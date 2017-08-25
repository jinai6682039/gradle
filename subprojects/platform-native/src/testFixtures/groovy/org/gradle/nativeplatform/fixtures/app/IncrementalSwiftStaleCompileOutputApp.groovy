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

class IncrementalSwiftStaleCompileOutputApp extends IncrementalApp {
    private final greeter = new SwiftGreeter()
    private final sum = new SwiftSum()
    private final multiply = new SwiftMultiply()
    private final main = new SwiftMain(greeter, sum)

    List<IncrementalApp.Transform> transforms = [
        identity(greeter),
        rename(sum),
        delete(multiply),
        identity(main)
    ]

    @Override
    protected Set<String> getExpectedIntermediateFilenames() {
        return ['App.swiftmodule', 'App.swiftdoc', 'output-file-map.json']
    }

    @Override
    String getExpectedOutput() {
        main.expectedOutput
    }

    @Override
    String getExpectedAlternateOutput() {
        main.expectedOutput
    }

    protected Set<String> intermediateFilenames(SourceFile sourceFile) {
        def name = sourceFile.name.replace('.swift', '')
        ['.o', '~partial.swiftdoc', '~partial.swiftmodule'].collect { name + it }
    }
}
