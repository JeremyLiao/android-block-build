package com.modulea.apicenter.inner.api.moduleb;

import java.lang.String;

/**
 * Auto generate code, do not modify!!! */
public interface ModuleBInterface {
  void enterMain(android.app.Activity activity) throws
      com.jeremyliao.blockcore.exception.RemoteCallException;

  String getValue(int type) throws com.jeremyliao.blockcore.exception.RemoteCallException;

  com.jeremyliao.libcommon.bean.CommonTestBean getCommonBean() throws
      com.jeremyliao.blockcore.exception.RemoteCallException;

  com.jeremyliao.libcommon.bean.CommonTestBean getCommonBean1(
      com.jeremyliao.libcommon.bean.CommonTestParamBean paramBean) throws
      com.jeremyliao.blockcore.exception.RemoteCallException;
}
