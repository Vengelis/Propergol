package fr.vengelis.propergol.core.plugin;

import fr.vengelis.propergol.core.application.ApplicationType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropergolPlugin {

    String name();
    ApplicationType type();
    String version();
    boolean asDefaultConfig() default false;

}
