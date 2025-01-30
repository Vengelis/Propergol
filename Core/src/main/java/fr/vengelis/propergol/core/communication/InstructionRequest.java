package fr.vengelis.propergol.core.communication;

import fr.vengelis.propergol.core.communication.retention.Retention;

public class InstructionRequest<T> {

    public enum Handler {
        POSTGRE,
        REDIS,
        DEPLOYER,
        ;
    }

    private final Retention retention;
    private final Handler handler;
    private final HandlerService service;
    private final T data;

    public InstructionRequest(Retention retention, Handler handler, HandlerService service, T data) {
        this.retention = retention;
        this.handler = handler;
        this.service = service;
        this.data = data;
    }

    public Retention getRetention() {
        return retention;
    }

    public Handler getHandler() {
        return handler;
    }

    public HandlerService getService() {
        return service;
    }

    public T getData() {
        return data;
    }
}
