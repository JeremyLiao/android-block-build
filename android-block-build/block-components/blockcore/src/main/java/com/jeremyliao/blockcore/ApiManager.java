package com.jeremyliao.blockcore;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Pair;

import com.google.gson.Gson;
import com.jeremyliao.blockcommon.bean.RuntimeInfo;
import com.jeremyliao.blockcore.bean.InterfaceContainer;

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

    private static final String ASSETS_DATA_PATH = "block/impl_info.json";

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
    private RuntimeInfo runtimeInfo;
    private InterfaceContainer interfaceContainer;
    private Map<Class, Object> proxyCache = new HashMap<>();

    private ApiManager() {
    }

    private void loadAssets() {
        if (context == null) {
            throw new RuntimeException("ApiManager not init, call init first!");
        }
        AssetManager asset = context.getAssets();
        try {
            InputStream inputStream = asset.open(ASSETS_DATA_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String json = reader.readLine();
            runtimeInfo = gson.fromJson(json, RuntimeInfo.class);
            if (runtimeInfo != null) {
                interfaceContainer = new InterfaceContainer(runtimeInfo);
            }
            reader.close();
            inputStream.close();
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
                new InterfaceHandler());
        proxyCache.put(interfaceType, proxyInstance);
        return (T) proxyInstance;
    }

    private class InterfaceHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] objects) {
            try {
                String module = method.getName();
                RuntimeInfo.Info info = runtimeInfo.getInfoMap().get(module);
                String interfaceClassName = info != null ? info.getInterfaceClassName() : null;
                Class interfaceType = Class.forName(interfaceClassName);
                if (proxyCache.containsKey(interfaceType)) {
                    return proxyCache.get(interfaceType);
                }
                Object proxyInstance = Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{interfaceType},
                        new InterfaceMethodHandler(info));
                proxyCache.put(interfaceType, proxyInstance);
                return proxyInstance;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class InterfaceMethodHandler implements InvocationHandler {

        private final RuntimeInfo.Info info;

        InterfaceMethodHandler(RuntimeInfo.Info info) {
            this.info = info;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) {
            try {
                Class implType = Class.forName(info.getImplementClassName());
                Pair<Class<?>[], Object[]> realParameterTV = getRealParameterTypesAndValues(method, objects);
                Method calledMethod = implType.getDeclaredMethod(method.getName(), realParameterTV.first);
                Object target = interfaceContainer.getTarget(info.getModuleName());
                Object objReturn = calledMethod.invoke(target, realParameterTV.second);
                return getRealReturn(method, objReturn);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
