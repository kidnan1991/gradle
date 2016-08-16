/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.composite.internal;

import com.google.common.collect.Lists;
import org.gradle.BuildResult;
import org.gradle.api.Action;
import org.gradle.api.artifacts.DependencySubstitutions;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.internal.artifacts.ivyservice.dependencysubstitution.DefaultDependencySubstitutions;
import org.gradle.api.internal.artifacts.ivyservice.dependencysubstitution.DependencySubstitutionsInternal;
import org.gradle.initialization.GradleLauncher;
import org.gradle.internal.Factory;

import java.io.File;
import java.util.List;

public class DefaultIncludedBuild implements IncludedBuildInternal {
    private final File projectDir;
    private final Factory<GradleLauncher> gradleLauncherFactory;
    // TODO:DAZ Get rid of this once we remove the "old" TAPI-based composites
    private final Factory<GradleLauncher> nestedLauncherFactory;
    private final List<Action<? super DependencySubstitutions>> dependencySubstitutionActions = Lists.newArrayList();

    private GradleLauncher gradleLauncher;
    private DefaultDependencySubstitutions dependencySubstitutions;
    private String name;

    public DefaultIncludedBuild(File projectDir, Factory<GradleLauncher> launcherFactory, Factory<GradleLauncher> nestedLauncherFactory) {
        this.projectDir = projectDir;
        this.gradleLauncherFactory = launcherFactory;
        this.nestedLauncherFactory = nestedLauncherFactory;
    }

    public File getProjectDir() {
        return projectDir;
    }

    @Override
    public synchronized String getName() {
        if (name == null) {
            name = initialize().getRootProject().getName();
        }
        return name;
    }

    @Override
    public void dependencySubstitution(Action<? super DependencySubstitutions> action) {
        if (dependencySubstitutions != null) {
            throw new IllegalStateException("Cannot configure included build after dependency substitutions are resolved.");
        }
        dependencySubstitutionActions.add(action);
    }

    public DependencySubstitutionsInternal resolveDependencySubstitutions() {
        if (dependencySubstitutions == null) {
            dependencySubstitutions = DefaultDependencySubstitutions.forIncludedBuild(getName());

            for (Action<? super DependencySubstitutions> action : dependencySubstitutionActions) {
                action.execute(dependencySubstitutions);
            }
        }
        return dependencySubstitutions;
    }

    @Override
    public SettingsInternal initialize() {
        GradleLauncher gradleLauncher = getGradleLauncher();
        gradleLauncher.load();
        return gradleLauncher.getSettings();
    }

    @Override
    public GradleInternal configure() {
        GradleLauncher gradleLauncher = getGradleLauncher();
        gradleLauncher.getBuildAnalysis();
        return gradleLauncher.getGradle();
    }

    private GradleLauncher getGradleLauncher() {
        if (gradleLauncher == null) {
            gradleLauncher = gradleLauncherFactory.create();
        }
        return gradleLauncher;
    }

    @Override
    public BuildResult execute(Iterable<String> tasks) {
        GradleLauncher gradleLauncher = nestedLauncherFactory.create();
        gradleLauncher.getGradle().getStartParameter().setTaskNames(tasks);
        return gradleLauncher.run();
    }

    @Override
    public String toString() {
        return String.format("includedBuild[%s]", projectDir.getPath());
    }
}