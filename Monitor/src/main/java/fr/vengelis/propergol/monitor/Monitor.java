package fr.vengelis.propergol.monitor;

import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.application.ApplicationType;
import io.micronaut.runtime.Micronaut;

public class Monitor {

    public static void main(String[] args) {
        Core core = new Core(ApplicationType.REPOSITORY);
        core.boot();
        Micronaut.run(Monitor.class, args);
    }

}
