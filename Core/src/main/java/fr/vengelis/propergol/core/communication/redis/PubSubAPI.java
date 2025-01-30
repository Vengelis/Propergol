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
            ConsoleLogger.printLine(Level.FINEST, "Redis publish error : " + e.getMessage());
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public void subscribe(String channel, IPacketsReceiver receiver) {
        Thread thread = new Thread(() -> {
            boolean disconnected = false;
            while(true) {
                try (Jedis jedis = RedisConnection.getJedis()) {
                    if(disconnected) {
                        disconnected = false;
                        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("redis-pubsub-reconnected"));
                    }
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
//                ConsoleLogger.printStacktrace(e);
                    if(e.getMessage().contains("Unexpected end of stream.")) {
                        disconnected = true;
                        ConsoleLogger.printLine(Level.SEVERE, "SUB : " + e.getMessage());
                        ConsoleLogger.printLine(Level.SEVERE, LanguageManager.translate("redis-pubsub-disconnected"));
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "sub (c = " + channel + ")");
        thread.start();
    }

    public void psubscribe(String pattern, IPatternReceiver receiver) {
        Thread thread = new Thread(() -> {
            boolean disconnected = false;
            while (true) {
                try (Jedis jedis = RedisConnection.getJedis()) {
                    if(disconnected) {
                        disconnected = false;
                        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("redis-pubsub-reconnected"));
                    }
                    jedis.psubscribe(new JedisPubSub() {

                        @Override
                        public void onPMessage(String pattern, String channel, String message) {
                            receiver.receive(pattern, channel, message);
                        }

                    }, pattern);
                } catch (Exception e) {
//                ConsoleLogger.printStacktrace(e);
                    if(e.getMessage().contains("Unexpected end of stream.")) {
                        disconnected = true;
                        ConsoleLogger.printLine(Level.SEVERE, "SUB : " + e.getMessage());
                        ConsoleLogger.printLine(Level.SEVERE, LanguageManager.translate("redis-pubsub-disconnected"));
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "sub (p = " + pattern + ")");
        thread.start();
    }
}
