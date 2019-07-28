package com.jeremyliao.blockplugin.transform;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.google.gson.Gson;
import com.jeremyliao.blockcommon.bean.CompileInfo;
import com.jeremyliao.blockcommon.bean.InterfaceInfo;
import com.jeremyliao.blockcommon.bean.VersionInfo;
import com.jeremyliao.blockplugin.exception.VersionErrorException;
import com.jeremyliao.blockplugin.utils.GradleUtils;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by liaohailiang on 2019/2/21.
 */
public class VersionCheckTransform extends Transform {

    private static final String TAG = "[VersionCheckTransform]";
    private static final String EXPORT_INFO_PATH = "META-INF/block/export/";
    private static final String VERSION_INFO_PATH = "META-INF/block/version/";

    private Gson gson = new Gson();
    private Map<String, CompileInfo> providerInfoMap = new HashMap<>();
    private Map<String, VersionInfo> consumerInfoMap = new HashMap<>();

    @Override
    public String getName() {
        return "VersionCheckTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        processInput(transformInvocation);
        checkVersion();
        copyInputToOutput(transformInvocation);
    }

    private void copyInputToOutput(TransformInvocation transformInvocation) throws IOException {
        for (TransformInput input : transformInvocation.getInputs()) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
            for (JarInput jarInput : input.getJarInputs()) {
                String destName = jarInput.getName();
                File dest = transformInvocation.getOutputProvider().getContentLocation(destName,
                        jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                FileUtils.copyFile(jarInput.getFile(), dest);
            }
        }
    }

    private void processInput(TransformInvocation transformInvocation) throws IOException {
        for (TransformInput input : transformInvocation.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                File file = jarInput.getFile();
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (isBlockExportEntry(entry)) {
                        String content = GradleUtils.getContent(jarFile, entry);
                        CompileInfo compileInfo = gson.fromJson(content, CompileInfo.class);
                        providerInfoMap.put(compileInfo.getModule(), compileInfo);
                    } else if (isBlockVersionEntry(entry)) {
                        String content = GradleUtils.getContent(jarFile, entry);
                        VersionInfo versionInfo = gson.fromJson(content, VersionInfo.class);
                        consumerInfoMap.put(versionInfo.getModule(), versionInfo);
                    }
                }
            }
        }
    }

    private void checkVersion() {
        for (VersionInfo versionInfo : consumerInfoMap.values()) {
            Map<String, Integer> versionMap = versionInfo.getVersionMap();
            for (String calledModule : versionMap.keySet()) {
                if (providerInfoMap.containsKey(calledModule)) {
                    int needVersion = versionMap.get(calledModule);
                    InterfaceInfo interfaceInfo = providerInfoMap.get(calledModule).getInterfaceInfo();
                    if (interfaceInfo == null) {
                        System.err.println(TAG + "interface provider not found for module: " + calledModule);
                    } else {
                        int realVersion = interfaceInfo.getVersion();
                        if (needVersion != realVersion) {
                            throw new VersionErrorException(versionInfo.getModule(), calledModule,
                                    needVersion, realVersion);
                        }
                    }
                } else {
                    System.err.println(TAG + "interface provider not found for module: " + calledModule);
                }
            }
        }
    }

    private boolean isBlockExportEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().startsWith(EXPORT_INFO_PATH);
    }

    private boolean isBlockVersionEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().startsWith(VERSION_INFO_PATH);
    }
}
