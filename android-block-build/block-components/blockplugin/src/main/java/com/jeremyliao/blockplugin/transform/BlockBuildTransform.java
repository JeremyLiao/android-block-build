package com.jeremyliao.blockplugin.transform;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.builder.model.AndroidProject;
import com.android.utils.FileUtils;
import com.google.gson.Gson;

import com.jeremyliao.blockcommon.bean.CompileInfo;
import com.jeremyliao.blockcommon.bean.MessageInfo;
import com.jeremyliao.blockcommon.bean.MessageInfo.*;
import com.jeremyliao.blockcommon.bean.RuntimeInfo;
import com.jeremyliao.blockcommon.bean.BeanInfo;
import com.jeremyliao.blockcommon.bean.BeanInfo.*;
import com.jeremyliao.blockcommon.bean.InterfaceInfo;
import com.jeremyliao.blockcommon.bean.InterfaceInfo.*;
import com.jeremyliao.blockcommon.bean.VersionInfo;
import com.jeremyliao.blockplugin.extension.BlockExtension;
import com.jeremyliao.blockplugin.utils.GradleUtils;
import com.jeremyliao.blockplugin.utils.ZipUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.lang.model.element.Modifier;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class BlockBuildTransform extends Transform {

    private static final String TAG = "[BlockBuildTransform]";
    private static final String EXPORT_INFO_PATH = "META-INF/block/export/";
    private static final String VERSION_INFO_PATH = "META-INF/block/version/";
    private static final String ASSETS_PATH = "block/";
    private static final String ASSETS_IMPL_FILE = "impl_info.json";
    private static final String API_MANAGER_CLASS_NAME = "com.jeremyliao.blockcore.ApiManager";
    private static final String REMOTE_EXCEPTION_CLASS_NAME = "com.jeremyliao.blockcore.exception.RemoteCallException";

    private final Project project;
    private final BlockExtension extension;
    private Gson gson = new Gson();
    private Map<String, CompileInfo> compileInfoMap = new HashMap<>();
    private Map<String, String> oriToNewBeanMap = new HashMap<>();
    private Map<String, String> newToOriBeanMap = new HashMap<>();
    private RuntimeInfo runtimeInfo = new RuntimeInfo();
    private VersionInfo versionInfo = new VersionInfo();

    public BlockBuildTransform(Project project, BlockExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    @Override
    public String getName() {
        return BlockBuildTransform.class.getSimpleName();
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
        System.out.println(TAG + "start transform");
        versionInfo.setModule(extension.getModule());
        clean();
        scanning(transformInvocation);
        output();
        writeVersionInfo();
    }

    private void scanning(TransformInvocation transformInvocation) throws IOException {
        for (TransformInput input : transformInvocation.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                File file = jarInput.getFile();
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (isExportInfoEntry(entry)) {
                        System.out.println(TAG + "entry name: " + entry.getName());
                        String moduleName = getSuffix(entry.getName(), "/");
                        System.out.println(TAG + "moduleName: " + moduleName);
                        String content = GradleUtils.getContent(jarFile, entry);
                        System.out.println(TAG + "content: " + content);
                        if (!moduleName.equals(extension.getModule()) && !compileInfoMap.containsKey(moduleName)) {
                            CompileInfo compileInfo = gson.fromJson(content, CompileInfo.class);
                            compileInfoMap.put(moduleName, compileInfo);
                            versionInfo.getVersionMap().put(moduleName, compileInfo.getInterfaceInfo().getVersion());
                            runtimeInfo.getInfoMap().put(lowerCaseFirstLetter(compileInfo.getModule()),
                                    RuntimeInfo.createInfo(
                                            compileInfo.getModule(),
                                            getInterfaceCompleteClassName(compileInfo.getInterfaceInfo()),
                                            compileInfo.getInterfaceInfo().getImplementClassName(),
                                            compileInfo.getInterfaceInfo().getVersion()));
                        }
                    }
                }
            }
        }
    }

    private void clean() {
        cleanDirectory(getSourcePath());
        cleanDirectory(getAssetsPath());
        cleanDirectory(getResourcesPath());
    }

    private void cleanDirectory(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.mkdirs();
            } else {
                FileUtils.deleteDirectoryContents(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void output() {
        File mainPath = new File(getSourcePath());
        if (compileInfoMap.size() > 0) {
            for (CompileInfo compileInfo : compileInfoMap.values()) {
                generateBeanClass(mainPath, compileInfo);
                generateInterfaceClass(mainPath, compileInfo.getInterfaceInfo());
//                generateMessageClass(mainPath, compileInfo.getMessageInfo());

            }
            generateGlobalApiClass(mainPath);
            generateApiCenterClass(mainPath);
//            generateGlobalMessageClass(mainPath);
//            generateMessageCenterClass(mainPath);
        }
        runtimeInfo.setNewToOriBeanMap(newToOriBeanMap);
//        runtimeInfo.setOriToNewBeanMap(oriToNewBeanMap);
        writeToAssets();
    }

    private void writeVersionInfo() {
        File dir = new File(getResourcesPath(), VERSION_INFO_PATH);
        if (dir.isFile()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, extension.getModule());
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            writer.write(gson.toJson(versionInfo));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            GradleUtils.safeClose(writer);
        }
    }

    private void generateApiCenterClass(File mainPath) {
        String className = "ApiCenter";
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Auto generate code, do not modify!!!");
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        String returnTypeStr = getRootPackage() + ".apicenter.inner." + "GlobalApi";
        Type returnType = getType(returnTypeStr);
        if (returnType != null) {
            methodBuilder.returns(returnType);
        } else {
            methodBuilder.returns(TypeVariableName.get(returnTypeStr));
        }
        methodBuilder.addCode(CodeBlock.builder()
                .addStatement("return $L.get().convert($L.class)", API_MANAGER_CLASS_NAME, returnTypeStr)
                .build());
        builder.addMethod(methodBuilder.build());
        String packageName = getRootPackage() + ".apicenter";
        System.out.println(TAG + "packageName: " + packageName);
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(mainPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateMessageCenterClass(File mainPath) {
        String className = "MessageCenter";
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Auto generate code, do not modify!!!");
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        String returnTypeStr = getRootPackage() + ".apicenter.inner." + "GlobalMessage";
        Type returnType = getType(returnTypeStr);
        if (returnType != null) {
            methodBuilder.returns(returnType);
        } else {
            methodBuilder.returns(TypeVariableName.get(returnTypeStr));
        }
        methodBuilder.addCode(CodeBlock.builder()
                .addStatement("return $L.get().convert($L.class)", API_MANAGER_CLASS_NAME, returnTypeStr)
                .build());
        builder.addMethod(methodBuilder.build());
        String packageName = getRootPackage() + ".apicenter";
        System.out.println(TAG + "packageName: " + packageName);
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(mainPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateGlobalApiClass(File mainPath) {
        String className = "GlobalApi";
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Auto generate code, do not modify!!!");
        Set<String> modules = compileInfoMap.keySet();
        for (String module : modules) {
            CompileInfo compileInfo = compileInfoMap.get(module);
            MethodSpec.Builder methodBuilder = MethodSpec
                    .methodBuilder(compileInfo.getInterfaceInfo().getModule().toLowerCase())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            String methodReturn = getInterfaceCompleteClassName(compileInfo.getInterfaceInfo());
            Type returnType = getType(methodReturn);
            if (returnType != null) {
                methodBuilder.returns(returnType);
            } else {
                methodBuilder.returns(TypeVariableName.get(methodReturn));
            }
            methodBuilder.addException(TypeVariableName.get(REMOTE_EXCEPTION_CLASS_NAME));
            builder.addMethod(methodBuilder.build());
        }
        String packageName = getRootPackage() + ".apicenter.inner";
        System.out.println(TAG + "packageName: " + packageName);
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(mainPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateGlobalMessageClass(File mainPath) {
        String className = "GlobalMessage";
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Auto generate code, do not modify!!!");
        Set<String> modules = compileInfoMap.keySet();
        for (String module : modules) {
            String methodName = lowerCaseFirstLetter(module);
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            String methodReturn = getMessageInterfaceCompleteClassName(methodName);
            Type returnType = getType(methodReturn);
            if (returnType != null) {
                methodBuilder.returns(returnType);
            } else {
                methodBuilder.returns(TypeVariableName.get(methodReturn));
            }
            builder.addMethod(methodBuilder.build());
        }
        String packageName = getRootPackage() + ".apicenter.inner";
        System.out.println(TAG + "packageName: " + packageName);
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(mainPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateInterfaceClass(File mainPath, InterfaceInfo interfaceInfo) {
        if (interfaceInfo == null) {
            return;
        }
        TypeSpec.Builder builder = TypeSpec
                .interfaceBuilder(interfaceInfo.getInterfaceName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Auto generate code, do not modify!!!");
        List<InterfaceMethodInfo> methodInfos = interfaceInfo.getMethodInfos();
        if (methodInfos != null && methodInfos.size() > 0) {
            for (InterfaceMethodInfo methodInfo : methodInfos) {
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                String returnType = methodInfo.getReturnType();
                if (oriToNewBeanMap.containsKey(returnType)) {
                    returnType = oriToNewBeanMap.get(returnType);
                }
                Type type = getType(returnType);
                if (type != null) {
                    methodBuilder.returns(type);
                } else {
                    methodBuilder.returns(TypeVariableName.get(returnType));
                }
                methodBuilder.addException(TypeVariableName.get(REMOTE_EXCEPTION_CLASS_NAME));
                List<String> params = methodInfo.getParams();
                List<String> paramTypes = methodInfo.getParamTypes();
                if (params != null && params.size() > 0) {
                    for (int i = 0; i < params.size(); i++) {
                        String param = params.get(i);
                        String pType = paramTypes.get(i);
                        if (oriToNewBeanMap.containsKey(pType)) {
                            pType = oriToNewBeanMap.get(pType);
                        }
                        Type paramType = getType(pType);
                        if (paramType != null) {
                            methodBuilder.addParameter(ParameterSpec.builder(
                                    paramType,
                                    param).build());
                        } else {
                            methodBuilder.addParameter(ParameterSpec.builder(
                                    TypeVariableName.get(pType),
                                    param).build());
                        }
                    }
                }
                builder.addMethod(methodBuilder.build());
            }
        }
        String packageName = getRootPackage()
                + ".apicenter.inner.api."
                + interfaceInfo.getModule().toLowerCase();
        System.out.println(TAG + "packageName: " + packageName);
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(mainPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateBeanClass(File mainPath, CompileInfo compileInfo) {
        if (compileInfo == null) {
            return;
        }
        List<BeanInfo> beanInfos = compileInfo.getBeanInfos();
        if (beanInfos == null || beanInfos.size() == 0) {
            return;
        }
        String packageName = getRootPackage()
                + ".apicenter.inner.bean."
                + compileInfo.getModule().toLowerCase();
        System.out.println(TAG + "packageName: " + packageName);
        //生成新的Bean的classname
        for (BeanInfo beanInfo : beanInfos) {
            String oriClassName = beanInfo.getClassName();
            String beanName = oriClassName.substring(oriClassName.lastIndexOf(".") + 1,
                    oriClassName.length());
            System.out.println(TAG + "beanName: " + beanName);
            String newClassName = packageName + "." + beanName;
            oriToNewBeanMap.put(oriClassName, newClassName);
            newToOriBeanMap.put(newClassName, oriClassName);
        }
        for (BeanInfo beanInfo : beanInfos) {
            String oriClassName = beanInfo.getClassName();
            String beanName = oriClassName.substring(oriClassName.lastIndexOf(".") + 1,
                    oriClassName.length());
            TypeSpec.Builder builder = TypeSpec.classBuilder(beanName)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("Auto generate code, do not modify!!!");
            List<FieldInfo> fieldInfos = beanInfo.getFieldInfos();
            for (FieldInfo fieldInfo : fieldInfos) {
                String returnType = fieldInfo.getReturnType();
                if (oriToNewBeanMap.containsKey(returnType)) {
                    //如果是自定义bean，则进行替换
                    returnType = oriToNewBeanMap.get(returnType);
                }
                Type type = getType(returnType);
                FieldSpec.Builder fieldBuilder = null;
                if (type != null) {
                    fieldBuilder = FieldSpec.builder(type, fieldInfo.getName(), Modifier.PUBLIC);
                } else {
                    fieldBuilder = FieldSpec.builder(
                            TypeVariableName.get(returnType),
                            fieldInfo.getName(), Modifier.PUBLIC);
                }
                builder.addField(fieldBuilder.build());
            }
            //生成java
            try {
                JavaFile.builder(packageName, builder.build())
                        .build()
                        .writeTo(mainPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateMessageClass(File mainPath, MessageInfo messageInfo) {
        if (messageInfo == null) {
            return;
        }
        String interfaceClassName = getMessageClassName(messageInfo.getModule());
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(interfaceClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Auto generate code, do not modify!!!");
        List<MessageFieldInfo> messageFieldInfos = messageInfo.getMessageFieldInfos();
        if (messageFieldInfos != null && messageFieldInfos.size() > 0) {
            for (MessageFieldInfo messageFieldInfo : messageFieldInfos) {
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(messageFieldInfo.getName())
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                Type type = getType(messageFieldInfo.getType());
                if (type != null) {
                    methodBuilder.returns(type);
                } else {
                    methodBuilder.returns(TypeVariableName.get(messageFieldInfo.getType()));
                }
                builder.addMethod(methodBuilder.build());
            }
        }
        String packageName = getRootPackage() + ".apicenter.inner.message";
        System.out.println(TAG + "packageName: " + packageName);
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(mainPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToAssets() {
        File assetsDir = new File(getAssetsPath(), ASSETS_PATH + extension.getModule());
        if (assetsDir.isFile()) {
            assetsDir.delete();
        }
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }
        File assetsFile = new File(assetsDir, ASSETS_IMPL_FILE);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(assetsFile);
            writer.write(gson.toJson(runtimeInfo));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            GradleUtils.safeClose(writer);
        }
    }

    private void copyFiles() {
        File source = new File(getSourcePath());
        File dest = new File(getGeneratedDir());
        try {
            FileUtils.copyDirectoryContentToDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zipFiles() {
        File source = new File(getSourcePath());
        File target = new File(getModuleRootDir().getAbsolutePath() + "/block/source.zip");
        ZipUtils.zipDirectory(source, target);
        try {
            FileUtils.deleteDirectoryContents(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Type getType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private String getInterfaceCompleteClassName(InterfaceInfo interfaceInfo) {
        return getRootPackage()
                + ".apicenter.inner.api."
                + interfaceInfo.getModule().toLowerCase()
                + "."
                + interfaceInfo.getInterfaceName();
    }

    private String getMessageInterfaceCompleteClassName(String name) {
        return getRootPackage()
                + ".apicenter.inner.message."
                + getMessageClassName(name);
    }

    private String getInterfaceClassName(String name) {
        return upperCaseFirstLetter(name) + "Api";
    }

    private String getMessageClassName(String name) {
        return upperCaseFirstLetter(name) + "Message";
    }

    private String upperCaseFirstLetter(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String lowerCaseFirstLetter(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private String getMainPath() {
        return FileUtils.join(getModuleRootDir().getAbsolutePath(),
                "src-gen", "main");
    }

    private String getSourcePath() {
        return FileUtils.join(getMainPath(), "java");
    }

    private String getAssetsPath() {
        return FileUtils.join(getMainPath(), "assets");
    }

    private String getResourcesPath() {
        return FileUtils.join(getMainPath(), "resources");
    }

    private File getModuleRootDir() {
        return project.getBuildDir().getParentFile();
    }

    private String getGeneratedDir() {
        return FileUtils.join(project.getBuildDir().getPath(),
                AndroidProject.FD_GENERATED,
                "source",
                "apt",
                "debug");
    }

    private String getRootPackage() {
        return "com." + extension.getModule();
    }

    private boolean isExportInfoEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().startsWith(EXPORT_INFO_PATH);
    }

    private void deleteDir(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
        dir.delete();
    }

    private String getSuffix(String s, String splitter) {
        int i = s.lastIndexOf(splitter);
        if (i < 0) {
            return s;
        } else {
            return s.substring(i + splitter.length());
        }
    }
}
