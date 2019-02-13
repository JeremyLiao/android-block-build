package com.jeremyliao.blockplugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.AndroidSourceSet;
import com.jeremyliao.blockplugin.exception.ExtensionConfigException;
import com.jeremyliao.blockplugin.extension.BlockExtension;
import com.jeremyliao.blockplugin.transform.BlockBuildTransform;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.HashMap;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class BlockPlugin implements Plugin<Project> {

    private static final String TAG = "[BlockPlugin]";
    private static final String EXT_NAME = "Block";

    private boolean enableResolveMode = false;

    @Override
    public void apply(Project project) {
        System.out.println(TAG + project);
        System.out.println(TAG + "getRootDir: " + project.getRootDir());
        System.out.println(TAG + "getBuildDir: " + project.getBuildDir());

        enableResolveMode = Boolean.parseBoolean((String) project.property("enableResolveMode"));
        System.out.println(TAG + "enableResolveMode: " + enableResolveMode);

        BlockExtension extension = project.getExtensions().create(EXT_NAME, BlockExtension.class);
        HashMap<String, String> plugin = new HashMap<>();
        if (enableResolveMode) {
            plugin.put("plugin", "com.android.application");
        } else {
            plugin.put("plugin", "com.android.library");
        }
        project.apply(plugin);

        if (enableResolveMode) {
            addTransform(project, extension);
        }
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                if (enableResolveMode) {
                    checkExtension(extension);
                    project.dependencies(extension.getDependencies());
                }
            }
        });
    }

    private void addTransform(Project project, BlockExtension extension) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        System.out.println(TAG + "extension: " + extension);
        appExtension.registerTransform(new BlockBuildTransform(project, extension));
    }

    private void checkExtension(BlockExtension extension) {
        if (extension.getPackageName() == null || extension.getPackageName().length() == 0) {
            throw new ExtensionConfigException("Package name not set in extension!");
        }
        if (extension.getDependencies() == null) {
            throw new ExtensionConfigException("Dependencies not set in extension!");
        }
    }
}
