package fr.vengelis.propergol.api.plugin;

import fr.vengelis.propergol.api.app.ApplicationType;

public @interface PropergolPlugin {

    String name();
    ApplicationType type();
    String version();

}
