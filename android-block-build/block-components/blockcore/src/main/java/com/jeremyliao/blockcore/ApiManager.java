package com.jeremyliao.blockcore;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Pair;

import com.google.gson.Gson;
import com.jeremyliao.blockcommon.bean.RuntimeInfo;
import com.jeremyliao.blockcore.exception.RemoteCallException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liaohailiang on 2019/1/29.
 */
public final class ApiManager {

    private static final String ASSETS_DATA_ROOT_PATH = "block";
    private static final String ASSETS_DATA_FILE_PATH = "impl_info.json";

    public static void init(Context context) {
        ApiManager.get().context = context.getApplicationContext();
        ApiManager.get().loadAssets();
    }

    private static class SingletonHolder {
        private static final ApiManager API_MANAGER = new ApiManager();
    }

    public static ApiManager get() {
        return SingletonHolder.API_MANAGER;
    }

    private Context context;
    private Gson gson = new Gson();
    private Map<String, RuntimeInfo> runtimeInfoMap = new HashMap<>();
    private Map<Class, Object> proxyCache = new HashMap<>();
    private Map<String, Object> implTargetCache = new HashMap<>();

    private ApiManager() {
    }

    private void loadAssets() {
        if (context == null) {
            throw new RuntimeException("ApiManager not init, call init first!");
        }
        AssetManager asset = context.getAssets();
        try {
            String[] list = asset.list(ASSETS_DATA_ROOT_PATH);
            if (list != null && list.length > 0) {
                for (String module : list) {
                    String path = ASSETS_DATA_ROOT_PATH + "/" + module + "/" + ASSETS_DATA_FILE_PATH;
                    InputStream inputStream = asset.open(path);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String json = reader.readLine();
                    RuntimeInfo runtimeInfo = gson.fromJson(json, RuntimeInfo.class);
                    runtimeInfoMap.put(module, runtimeInfo);
                    reader.close();
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized <T> T convert(Class<T> interfaceType) {
        if (proxyCache.containsKey(interfaceType)) {
            return (T) proxyCache.get(interfaceType);
        }
        Object proxyInstance = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{interfaceType},
                new InterfaceHandler(getModuleName(interfaceType)));
        proxyCache.put(interfaceType, proxyInstance);
        return (T) proxyInstance;
    }

    private String getModuleName(Class<?> interfaceType) {
        String canonicalName = interfaceType.getCanonicalName();
        int firstDotIndex = canonicalName.indexOf(".");
        String name = canonicalName.substring(firstDotIndex + 1);
        int secondDotIndex = name.indexOf(".");
        return name.substring(0, secondDotIndex);
    }

    private class InterfaceHandler implements InvocationHandler {

        private final String module;

        public InterfaceHandler(String module) {
            this.module = module;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws RemoteCallException {
            if (!runtimeInfoMap.containsKey(module)) {
                return null;
            }
            RuntimeInfo runtimeInfo = runtimeInfoMap.get(module);
            try {
                String calledModule = method.getName();
                RuntimeInfo.Info info = runtimeInfo.getInfoMap().get(calledModule);
                String interfaceClassName = info != null ? info.getInterfaceClassName() : null;
                Class interfaceType = Class.forName(interfaceClassName);
                if (proxyCache.containsKey(interfaceType)) {
                    return proxyCache.get(interfaceType);
                }
                Object proxyInstance = Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{interfaceType},
                        new InterfaceMethodHandler(runtimeInfo, info));
                proxyCache.put(interfaceType, proxyInstance);
                return proxyInstance;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RemoteCallException("Module not found!", e);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RemoteCallException("Unknown error!", e);
            }
        }
    }

    private class InterfaceMethodHandler implements InvocationHandler {

        private final RuntimeInfo runtimeInfo;
        private final RuntimeInfo.Info info;

        InterfaceMethodHandler(RuntimeInfo runtimeInfo, RuntimeInfo.Info info) {
            this.runtimeInfo = runtimeInfo;
            this.info = info;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws RemoteCallException {
            try {
                Class implType = Class.forName(info.getImplementClassName());
                Pair<Class<?>[], Object[]> realParameterTV = getRealParameterTypesAndValues(method, objects);
                Method calledMethod = implType.getDeclaredMethod(method.getName(), realParameterTV.first);
                Object target = getTarget(info.getModuleName());
                Object objReturn = calledMethod.invoke(target, realParameterTV.second);
                return getRealReturn(method, objReturn);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RemoteCallException("Module not found!", e);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new RemoteCallException("No such method!", e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RemoteCallException("Unknown error!", e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RemoteCallException("Unknown error!", e);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RemoteCallException("Unknown error!", e);
            }
        }

        private Object getTarget(String module) {
            if (module == null || module.length() == 0) {
                return null;
            }
            if (implTargetCache.containsKey(module)) {
                return implTargetCache.get(module);
            }
            if (runtimeInfo == null) {
                return null;
            }
            Map<String, RuntimeInfo.Info> infoMap = runtimeInfo.getInfoMap();
            if (infoMap == null) {
                return null;
            }
            if (!infoMap.containsKey(module)) {
                return null;
            }
            try {
                String className = infoMap.get(module).getImplementClassName();
                Class type = Class.forName(className);
                Object target = type.newInstance();
                implTargetCache.put(module, target);
                return target;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private Pair<Class<?>[], Object[]> getRealParameterTypesAndValues(Method method, Object[] objects)
                throws ClassNotFoundException {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes == null || parameterTypes.length == 0) {
                return Pair.create(parameterTypes, objects);
            }
            Class<?>[] realParameterTypes = new Class<?>[parameterTypes.length];
            Object[] realObjects = new Object[objects.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                String typeCanonicalName = type.getCanonicalName();
                if (runtimeInfo.getNewToOriBeanMap().containsKey(typeCanonicalName)) {
                    Class<?> realType = Class.forName(
                            runtimeInfo.getNewToOriBeanMap().get(typeCanonicalName));
                    realParameterTypes[i] = realType;
                    //转化成real bean
                    String json = gson.toJson(objects[i]);
                    Object fromJson = gson.fromJson(json, realType);
                    realObjects[i] = fromJson;
                } else {
                    realParameterTypes[i] = type;
                    realObjects[i] = objects[i];
                }
            }
            return Pair.create(realParameterTypes, realObjects);
        }

        private Object getRealReturn(Method method, Object o) {
            if (method == null || o == null) {
                return null;
            }
            Class<?> returnType = method.getReturnType();
            if (returnType.isInstance(o)) {
                return o;
            } else {
                String json = gson.toJson(o);
                return gson.fromJson(json, returnType);
            }
        }
    }
}
