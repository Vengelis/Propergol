package fr.vengelis.propergol.core.application.handler.impl;

public interface BootHandler {

    enum Step {
        BEFORE_API,
        BEFORE_PLUGINS,
        AFTER_PLUGINS,
        LAST,
        ;
    }

    void process();
    Step getStep();
}
