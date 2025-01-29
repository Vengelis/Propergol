package fr.vengelis.propergol.core.communication.redis;

import fr.vengelis.propergol.core.communication.redis.instruction.PublishInstruction;
import fr.vengelis.propergol.core.language.LanguageManager;
import fr.vengelis.propergol.core.utils.ConsoleLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

public class PubSubAPI {

    public RedisResult<Object> tryHelloWorld() {
        try (Jedis jedis = RedisConnection.getJedis()) {
            jedis.publish("AFTERBURNER-HELLOWORLD", "Hi guy's :D");
            return new RedisResult<>(RedisResult.Type.SUCCESS);
        } catch (Exception e) {
//            ConsoleLogger.printStacktrace(e);
            ConsoleLogger.printLinesBox(Level.SEVERE, new String[]{
                    LanguageManager.translate("redis-not-op")
            });
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public RedisResult<Object> publish(PublishInstruction instruction) {
        try (Jedis jedis = RedisConnection.getJedis()) {
            jedis.publish(instruction.channel(), instruction.message());
            return new RedisResult<>(RedisResult.Type.SUCCESS);
        } catch (Exception e) {
//            ConsoleLogger.printStacktrace(e);
            ConsoleLogger.printLine(Level.SEVERE, e.getMessage());
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public void subscribe(String channel, IPacketsReceiver receiver) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = RedisConnection.getJedis()) {
                jedis.subscribe(new JedisPubSub() {

                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            receiver.receive(channel, message);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                }, channel);
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
            }
        }, "sub (c = " + channel + ")");
        thread.start();
    }

    public void psubscribe(String pattern, IPatternReceiver receiver) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = RedisConnection.getJedis()) {
                jedis.psubscribe(new JedisPubSub() {

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        receiver.receive(pattern, channel, message);
                    }

                }, pattern);
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
            }
        }, "sub (p = " + pattern + ")");
        thread.start();
    }
}
