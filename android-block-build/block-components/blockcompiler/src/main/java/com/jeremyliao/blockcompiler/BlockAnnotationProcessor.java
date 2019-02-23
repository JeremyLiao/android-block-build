package com.jeremyliao.blockcompiler;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.jeremyliao.blockbase.annotation.BeanExport;
import com.jeremyliao.blockbase.annotation.InterfaceExport;
import com.jeremyliao.blockbase.annotation.MessageExport;
import com.jeremyliao.blockbase.annotation.MessageType;
import com.jeremyliao.blockcommon.bean.BeanInfo;
import com.jeremyliao.blockcommon.bean.BeanInfo.*;
import com.jeremyliao.blockcommon.bean.CompileInfo;
import com.jeremyliao.blockcommon.bean.InterfaceInfo;
import com.jeremyliao.blockcommon.bean.InterfaceInfo.*;
import com.jeremyliao.blockcommon.bean.MessageInfo;
import com.jeremyliao.blockcommon.bean.MessageInfo.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Created by liaohailiang on 2019/1/24.
 */
@AutoService(Processor.class)
public class BlockAnnotationProcessor extends AbstractProcessor {

    private static final String TAG = "[BlockAnnotationProcessor]";
    private static final String EXPORT_INFO_PATH = "META-INF/block/export/";

    protected Filer filer;
    protected Types types;
    protected Elements elements;

    private Gson gson = new Gson();
    private Map<String, CompileInfo> compileInfoMap = new HashMap<>();
    private List<BeanInfo> beanInfos = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println(TAG + "start process!!!!");
        if (roundEnvironment.processingOver()) {
            writeConfigInfo();
        } else {
            process(roundEnvironment);
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(InterfaceExport.class.getCanonicalName());
        annotations.add(MessageExport.class.getCanonicalName());
        annotations.add(BeanExport.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void process(RoundEnvironment roundEnvironment) {
        processInterface(roundEnvironment);
        processMessage(roundEnvironment);
        processBean(roundEnvironment);
    }

    private void processInterface(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(InterfaceExport.class)) {
            System.out.println(TAG + "name: " + element.getSimpleName());
            if (element.getKind() == ElementKind.INTERFACE) {
                TypeElement typeElement = (TypeElement) element;
                InterfaceExport interfaceExport = typeElement.getAnnotation(InterfaceExport.class);
                String interfaceClassName = typeElement.getSimpleName().toString();
                String moduleName = interfaceExport.module();
                if (moduleName.length() == 0) {
                    PackageElement packageElement = elements.getPackageOf(element);
                    moduleName = packageElement.getQualifiedName().toString();
                }
                System.out.println(TAG + "moduleName: " + moduleName);
                InterfaceInfo interfaceInfo = new InterfaceInfo();
                interfaceInfo.setModule(moduleName);
                interfaceInfo.setVersion(interfaceExport.version());
                interfaceInfo.setInterfaceName(interfaceClassName);
                interfaceInfo.setImplementClassName(getImpl(interfaceExport));
                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                List<InterfaceMethodInfo> methodInfos = new ArrayList<>();
                for (Element element1 : enclosedElements) {
                    if (element1.getKind() == ElementKind.METHOD) {
                        ExecutableElement executableElement = (ExecutableElement) element1;
                        InterfaceMethodInfo methodInfo = new InterfaceMethodInfo();
                        methodInfo.setName(executableElement.getSimpleName().toString());
                        methodInfo.setReturnType(executableElement.getReturnType().toString());
                        for (VariableElement ve : executableElement.getParameters()) {
                            methodInfo.getParams().add(ve.getSimpleName().toString());
                            methodInfo.getParamTypes().add(ve.asType().toString());
                        }
                        methodInfos.add(methodInfo);
                    }
                }
                interfaceInfo.setMethodInfos(methodInfos);
                CompileInfo compileInfo = new CompileInfo();
                compileInfo.setModule(interfaceInfo.getModule());
                compileInfo.setInterfaceInfo(interfaceInfo);
                compileInfoMap.put(moduleName, compileInfo);
            }
        }
    }

    private void processMessage(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(MessageExport.class)) {
            System.out.println(TAG + "name: " + element.getSimpleName());
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                MessageExport messageExport = typeElement.getAnnotation(MessageExport.class);
                String moduleName = messageExport.name();
                if (moduleName.length() == 0) {
                    PackageElement packageElement = elements.getPackageOf(element);
                    moduleName = packageElement.getQualifiedName().toString();
                }
                System.out.println(TAG + "moduleName: " + moduleName);
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setModule(moduleName);
                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                List<MessageFieldInfo> messageFieldInfos = new ArrayList<>();
                for (Element element1 : enclosedElements) {
                    if (element1.getKind() == ElementKind.FIELD) {
                        VariableElement variableElement = (VariableElement) element1;
                        String variableName = variableElement.getSimpleName().toString();
                        Object variableValue = variableElement.getConstantValue();
                        String messageType = getMessageType(element1);
                        MessageFieldInfo messageFieldInfo = new MessageFieldInfo();
                        messageFieldInfo.setName(variableName);
                        if (variableValue instanceof String) {
                            messageFieldInfo.setValue((String) variableValue);
                        }
                        messageFieldInfo.setType(messageType);
                        messageFieldInfos.add(messageFieldInfo);
                    }
                }
                messageInfo.setMessageFieldInfos(messageFieldInfos);
                if (!compileInfoMap.containsKey(messageInfo.getModule())) {
                    CompileInfo compileInfo = new CompileInfo();
                    compileInfo.setModule(messageInfo.getModule());
                    compileInfo.setMessageInfo(messageInfo);
                    compileInfoMap.put(messageInfo.getModule(), compileInfo);
                } else {
                    compileInfoMap.get(messageInfo.getModule()).setMessageInfo(messageInfo);
                }
            }
        }
    }

    private void processBean(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BeanExport.class)) {
            System.out.println(TAG + "name: " + element.getSimpleName());
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                BeanInfo beanInfo = new BeanInfo();
                beanInfo.setClassName(typeElement.getQualifiedName().toString());
                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                List<FieldInfo> fieldInfos = new ArrayList<>();
                for (Element element1 : enclosedElements) {
                    if (element1.getKind() == ElementKind.FIELD) {
                        VariableElement variableElement = (VariableElement) element1;
                        String variableName = variableElement.getSimpleName().toString();
                        String returnType = variableElement.asType().toString();
                        FieldInfo fieldInfo = new FieldInfo();
                        fieldInfo.setName(variableName);
                        fieldInfo.setReturnType(returnType);
                        fieldInfos.add(fieldInfo);
                    }
                }
                beanInfo.setFieldInfos(fieldInfos);
                beanInfos.add(beanInfo);
            }
        }
    }

    private void writeConfigInfo() {
        System.out.println(TAG + "generateConfigFiles");
        for (String name : compileInfoMap.keySet()) {
            CompileInfo compileInfo = compileInfoMap.get(name);
            compileInfo.setBeanInfos(beanInfos);
            writeToFile(EXPORT_INFO_PATH + name, gson.toJson(compileInfo));
        }
    }

    private void writeToFile(String fileName, String content) {
        if (isEmpty(fileName) || isEmpty(content)) {
            return;
        }
        System.out.println(TAG + "writeToFile fileName: " + fileName + " content: " + content);
        try {
            FileObject res = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName);
            System.out.println(TAG + res.getName());
            OutputStream os = res.openOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF_8));
            writer.write(content);
            writer.flush();
            writer.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(TAG + e.toString());
        }
    }

    private String getImpl(InterfaceExport interfaceExport) {
        try {
            return interfaceExport.impl().getCanonicalName();
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            return typeMirror.toString();
        }
    }

    private String getMessageType(Element element) {
        List<? extends AnnotationMirror> annotationMirrors = elements.getAllAnnotationMirrors(element);
        if (annotationMirrors != null && annotationMirrors.size() > 0) {
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                if (MessageType.class.getCanonicalName().equals(annotationMirror.getAnnotationType().toString())) {
                    System.out.println(TAG + "annotationMirror: " + annotationMirror.getAnnotationType().toString());
                    if (annotationMirror.getElementValues() != null) {
                        for (AnnotationValue annotationValue : annotationMirror.getElementValues().values()) {
                            return annotationValue.getValue().toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    static boolean isEmpty(String path) {
        return path == null || path.length() == 0;
    }
}
