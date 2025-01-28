package fr.vengelis.propergol.core.application.handler;

import fr.vengelis.propergol.core.application.handler.impl.BootHandler;
import fr.vengelis.propergol.core.application.handler.impl.InitHandler;
import fr.vengelis.propergol.core.application.handler.impl.PreInitHandler;

import java.util.ArrayList;
import java.util.List;

public class HandlerRecorder {

    private static HandlerRecorder instance;
    private final List<PreInitHandler> preInitHandlers = new ArrayList<>();
    private final List<InitHandler> initHandlers = new ArrayList<>();
    private final List<BootHandler> bootHandlers = new ArrayList<>();

    public HandlerRecorder() {
        instance = this;
    }

    public void register(PreInitHandler handler) {
        preInitHandlers.add(handler);
    }

    public void register(InitHandler handler) {
        initHandlers.add(handler);
    }

    public void register(BootHandler handler) {
        bootHandlers.add(handler);
    }

    public void executePreInit() {
        preInitHandlers.forEach(PreInitHandler::process);
    }

    public void executeInit() {
        initHandlers.forEach(InitHandler::process);
    }

    public void executeBoot(BootHandler.Step step) {
        bootHandlers.stream()
                .filter(h -> h.getStep().equals(step))
                .forEach(BootHandler::process);
    }

    public static HandlerRecorder get() {
        return instance;
    }
}
