package com.jeremyliao.blockplugin.utils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.DependencyResult;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by liaohailiang on 2019/2/20.
 */
public class ProjectUtils {

    private ProjectUtils() {
    }

    public static Set<String> getDependencies(Project project) {
        Set<String> dependencies = new HashSet<>();
        Iterator<Configuration> iterator = project.getConfigurations().iterator();
        while (iterator.hasNext()) {
            Configuration configuration = iterator.next();
            if (configuration.getName().toLowerCase().contains("runtimeclasspath")) {
                Set<? extends DependencyResult> dependencyResults =
                        configuration.getIncoming().getResolutionResult().getRoot().getDependencies();
                for (DependencyResult dr : dependencyResults) {
                    dependencies.add(dr.getRequested().getDisplayName());
                }
            }
        }
        return dependencies;
    }
}
