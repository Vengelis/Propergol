package fr.vengelis.propergol.core.communication;

import java.util.Optional;

public class InstructionResponse<T> {

    public enum SystemResult {
        SENDED,
        RETAINED,
        FORGOTTEN,
        ;
    }

    private final SystemResult systemResult;
    private final Optional<T> result;

    public InstructionResponse(SystemResult systemResult) {
        this.systemResult = systemResult;
        this.result = Optional.empty();
    }

    public InstructionResponse(SystemResult systemResult, T result) {
        this.systemResult = systemResult;
        this.result = Optional.of(result);
    }

    public SystemResult getSystemResult() {
        return systemResult;
    }

    public Optional<T> getResult() {
        return result;
    }
}
