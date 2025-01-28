package fr.vengelis.propergol.api;

import java.io.File;
import java.net.URISyntaxException;

public class API {

    public static String WORKING_AREA = "";

    public void boot() {
        try {
            WORKING_AREA = new File(API.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
