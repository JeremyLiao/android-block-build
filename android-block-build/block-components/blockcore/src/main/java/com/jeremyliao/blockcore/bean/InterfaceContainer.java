package com.jeremyliao.blockcore.bean;

import com.jeremyliao.blockcommon.bean.RuntimeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liaohailiang on 2019/1/29.
 */
public class InterfaceContainer {

    private final RuntimeInfo runtimeInfo;
    private Map<String, Object> implTargetMap = new HashMap<>();

    public InterfaceContainer(RuntimeInfo runtimeInfo) {
        this.runtimeInfo = runtimeInfo;
    }

    public Object getTarget(String module) {
        if (module == null || module.length() == 0) {
            return null;
        }
        if (implTargetMap.containsKey(module)) {
            return implTargetMap.get(module);
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
            implTargetMap.put(module, target);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
