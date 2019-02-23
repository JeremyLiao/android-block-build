package com.jeremyliao.blockplugin.exception;

/**
 * Created by liaohailiang on 2019/2/21.
 */
public class VersionErrorException extends RuntimeException {

    public VersionErrorException(String fromModule, String targetModule, int needVersion, int realVersion) {
        super("Module version error, when " + fromModule + " call " + targetModule + ", need version: "
                + needVersion + ", find version: " + realVersion
                + ". You need to do 'assembleDependencies' in " + fromModule);
    }
}
