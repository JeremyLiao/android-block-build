package com.jeremyliao.blockplugin.plugins;

import com.android.build.gradle.AppExtension;
import com.jeremyliao.blockplugin.exception.ExtensionConfigException;
import com.jeremyliao.blockplugin.extension.BlockExtension;
import com.jeremyliao.blockplugin.transform.BlockBuildTransform;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class BlockPlugin implements Plugin<Project> {

    private static final String TAG = "[BlockPlugin]";
    private static final String EXT_NAME = "Block";

    private boolean isAssembleDependencies = false;

    @Override
    public void apply(Project project) {
        System.out.println(TAG + project);
        System.out.println(TAG + "getRootDir: " + project.getRootDir());
        System.out.println(TAG + "getBuildDir: " + project.getBuildDir());

        isAssembleDependencies = (boolean) project.property("assemble_dependencies");
        System.out.println(TAG + "isAssembleDependencies: " + isAssembleDependencies);

        BlockExtension extension = project.getExtensions().create(EXT_NAME, BlockExtension.class);
        HashMap<String, String> plugin = new HashMap<>();
        if (isAssembleDependencies) {
            plugin.put("plugin", "com.android.application");
        } else {
            plugin.put("plugin", "com.android.library");
        }
        project.apply(plugin);

        if (isAssembleDependencies) {
            addTransform(project, extension);
        }
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                addTask(project);
                resolveDependencies(project, extension);
            }
        });
    }

    private void resolveDependencies(Project project, BlockExtension extension) {
        if (isAssembleDependencies) {
            checkExtension(extension);
            project.dependencies(extension.getDependencies());
        }
    }

    private void addTask(Project project) {
        Task assembleDependencies = project.task("assembleDependencies");
        Set<Task> depends = project.getTasksByName("assembleDebug", true);
        System.out.println(TAG + "depends: " + depends.size());
        Iterator<Task> iterator = depends.iterator();
        if (iterator.hasNext()) {
            assembleDependencies.dependsOn(iterator.next());
        }
    }

    private void addTransform(Project project, BlockExtension extension) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        System.out.println(TAG + "extension: " + extension);
        appExtension.registerTransform(new BlockBuildTransform(project, extension));
    }

    private void checkExtension(BlockExtension extension) {
        if (extension.getModule() == null || extension.getModule().length() == 0) {
            throw new ExtensionConfigException("Package name not set in extension!");
        }
        if (extension.getDependencies() == null) {
            throw new ExtensionConfigException("Dependencies not set in extension!");
        }
    }
}
