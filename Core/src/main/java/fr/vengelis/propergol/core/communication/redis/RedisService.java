package fr.vengelis.propergol.core.communication.redis;

import fr.vengelis.propergol.core.communication.HandlerService;

public enum RedisService implements HandlerService {

    PUBLISH,
    SET,
    SETEX,
    DEL,
    GET,
    EX,
    GETKEYS,
    RECONNECT,
    CLOSE,
    ;

}
