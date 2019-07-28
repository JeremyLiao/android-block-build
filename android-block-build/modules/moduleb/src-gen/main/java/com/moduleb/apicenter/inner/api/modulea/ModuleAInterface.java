package com.moduleb.apicenter.inner.api.modulea;

import java.lang.String;

/**
 * Auto generate code, do not modify!!! */
public interface ModuleAInterface {
  void enterMain(android.app.Activity activity) throws
      com.jeremyliao.blockcore.exception.RemoteCallException;

  void enterGoodsPage(android.app.Activity activity) throws
      com.jeremyliao.blockcore.exception.RemoteCallException;

  String getValue(int type) throws com.jeremyliao.blockcore.exception.RemoteCallException;

  com.moduleb.apicenter.inner.bean.modulea.DemoBean getBean() throws
      com.jeremyliao.blockcore.exception.RemoteCallException;

  void setBean(com.moduleb.apicenter.inner.bean.modulea.DemoBean demoBean) throws
      com.jeremyliao.blockcore.exception.RemoteCallException;
}
