package com.jeremyliao.blockplugin.plugins;

import com.android.build.gradle.AppExtension;
import com.jeremyliao.blockplugin.transform.VersionCheckTransform;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by liaohailiang on 2019/2/21.
 */
public class VersionCheckPlugin implements Plugin<Project> {

    private static final String TAG = "[VersionCheckPlugin]";

    @Override
    public void apply(Project project) {
        addTransform(project);
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
            }
        });
    }

    private void addTransform(Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        appExtension.registerTransform(new VersionCheckTransform(project));
    }
}
