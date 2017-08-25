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

package org.gradle.nativeplatform.fixtures.app;

import org.gradle.integtests.fixtures.SourceFile;
import org.gradle.test.fixtures.file.TestFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class IncrementalApp {
    public final App app = new App();
    public final AlternateApp alternateApp = new AlternateApp();

    private final List<SourceFile> before = new ArrayList<SourceFile>();
    private final List<SourceFile> after = new ArrayList<SourceFile>();

    protected abstract List<Transform> getTransforms();

    public void applyChangesToProject(TestFile projectDir) {
        for (Transform transform : getTransforms()) {
            transform.apply(projectDir);
        }
    }

    public void writeToProject(TestFile projectDir) {
        app.writeToProject(projectDir);
    }

    public abstract String getExpectedOutput();
    public abstract String getExpectedAlternateOutput();
    protected abstract Set<String> getExpectedIntermediateFilenames();

    protected abstract Set<String> intermediateFilenames(SourceFile sourceFile);

    private abstract class AbstractApp extends SourceElement implements AppElement {
        public Set<String> getExpectedIntermediateFilenames() {
            Set result = new HashSet();
            for (SourceFile file : getFiles()) {
                result.addAll(intermediateFilenames(file));
            }

            result.addAll(IncrementalApp.this.getExpectedIntermediateFilenames());

            return result;
        }
    }

    class App extends AbstractApp {
        @Override
        public String getExpectedOutput() {
            return IncrementalApp.this.getExpectedOutput();
        }

        @Override
        public List<SourceFile> getFiles() {
            return before;
        }
    }

    class AlternateApp extends AbstractApp {
        @Override
        public String getExpectedOutput() {
            return getExpectedAlternateOutput();
        }

        @Override
        public List<SourceFile> getFiles() {
            return after;
        }
    }

    public interface Transform {
        void apply(TestFile projectDir);
    }

    protected Transform identity(SourceFileElement element) {
        assert element.getFiles().size() == 1;
        before.add(element.getSourceFile());
        after.add(element.getSourceFile());

        return new Transform() {
            @Override
            public void apply(TestFile projectDir) {
            }
        };
    }

    protected Transform modify(SourceFileElement beforeElement, SourceFileElement afterElement) {
        assert beforeElement.getFiles().size() == 1;
        assert afterElement.getFiles().size() == 1;
        assert beforeElement.getSourceSetName().equals(afterElement.getSourceSetName());
        final String sourceSetName = beforeElement.getSourceSetName();
        final SourceFile beforeFile = beforeElement.getSourceFile();
        final SourceFile afterFile = afterElement.getSourceFile();
        assert beforeFile.getPath().equals(afterFile.getPath());
        assert !beforeFile.getContent().equals(afterFile.getContent());
        before.add(beforeFile);
        after.add(afterFile);


        return new Transform() {
            @Override
            public void apply(TestFile projectDir) {
                TestFile file = projectDir.file(beforeFile.withPath("src/" + sourceSetName));
                file.assertExists();

                file.write(afterFile.getContent());
            }
        };
    }

    protected Transform delete(SourceFileElement beforeElement) {
        assert beforeElement.getFiles().size() == 1;
        final String sourceSetName = beforeElement.getSourceSetName();
        final SourceFile beforeFile = beforeElement.getSourceFile();
        before.add(beforeFile);

        return new Transform() {
            @Override
            public void apply(TestFile projectDir) {
                TestFile file = projectDir.file(beforeFile.withPath("src/" + sourceSetName));
                file.assertExists();

                file.delete();
            }
        };
    }

    protected Transform add(SourceFileElement afterElement) {
        assert afterElement.getFiles().size() == 1;
        final String sourceSetName = afterElement.getSourceSetName();
        final SourceFile afterFile = afterElement.getSourceFile();
        after.add(afterFile);

        return new Transform() {
            @Override
            public void apply(TestFile projectDir) {
                TestFile file = projectDir.file(afterFile.withPath("src/" + sourceSetName));

                file.assertDoesNotExist();

                afterFile.writeToDir(projectDir);
            }
        };
    }

    protected Transform rename(SourceFileElement beforeElement) {
        assert beforeElement.getFiles().size() == 1;
        final String sourceSetName = beforeElement.getSourceSetName();
        final SourceFile beforeFile = beforeElement.getSourceFile();
        before.add(beforeFile);
        final SourceFile afterFile = new SourceFile(beforeFile.getPath(), "renamed-" + beforeFile.getName(), beforeFile.getContent());
        after.add(afterFile);

        return new Transform() {
            @Override
            public void apply(TestFile projectDir) {
                TestFile file = projectDir.file(beforeFile.withPath("src/" + sourceSetName));

                file.assertExists();

                file.renameTo(projectDir.file(afterFile.withPath("src/" + sourceSetName)));
            }
        };
    }
}
