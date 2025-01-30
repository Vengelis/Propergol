package fr.vengelis.propergol.webconnect;

import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.application.ApplicationType;
import io.micronaut.runtime.Micronaut;

public class WebConnect {

    public static void main(String[] args) {
        Core core = new Core(ApplicationType.REPOSITORY);
        core.boot();
        Micronaut.run(WebConnect.class, args);
    }

}
