package fr.vengelis.propergol.core.application;

import fr.vengelis.propergol.core.utils.SystemInfoUtil;

public record Entity(String name,String ip, String mac, String hwid) {

    public static Entity buildHere() {
        return new Entity(
                SystemInfoUtil.getHostname(),
                SystemInfoUtil.getIpAddress(),
                SystemInfoUtil.getMacAddress(),
                SystemInfoUtil.getHWID());
    }

}
