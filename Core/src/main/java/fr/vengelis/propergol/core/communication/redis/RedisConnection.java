package fr.vengelis.propergol.core.communication.redis;

import fr.vengelis.propergol.core.utils.ConsoleLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.HashSet;
import java.util.Set;

public class RedisConnection {

    private static JedisPool pool;

    private static String lastHost;
    private static String lastUser;
    private static String lastPassword;
    private static int lastPort;
    private static int lastDatabase;
    private static int lastTimeout;

    public static void create(String host, String user, String password, int port, int database, int timeout) {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());

        lastHost = host;
        lastUser = user;
        lastPassword = password;
        lastPort = port;
        lastDatabase = database;
        lastTimeout = timeout;

        RedisConnection.pool = new JedisPool(new JedisPoolConfig(), host, port, timeout, password, database);
        pool.setMaxTotal(30);
        pool.setMaxIdle(30);
        Thread.currentThread().setContextClassLoader(previous);
    }

    public static Jedis getJedis() {
        return RedisConnection.pool.getResource();
    }

    public static RedisResult<Object> set(String key, String value) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.set(key, value);
            return new RedisResult<>(RedisResult.Type.SUCCESS);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public static RedisResult<Object> set(String key, String value, int seconds) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.setex(key, seconds, value);
            return new RedisResult<>(RedisResult.Type.SUCCESS);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public static RedisResult<Object> del(String key) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.del(key);
            return new RedisResult<>(RedisResult.Type.SUCCESS);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public static RedisResult<Object> get(String key) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            return new RedisResult<>(RedisResult.Type.SUCCESS, jedis.get(key));
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public static RedisResult<Object> expire(String key, Integer time) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.expire(key, time);
            return new RedisResult<>(RedisResult.Type.SUCCESS);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public static RedisResult<Set<String>> getKeys(String pattern) {
        try (Jedis jedis = RedisConnection.getJedis()) {
            Set<String> keys = new HashSet<>();

            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams scanParams = new ScanParams().match(pattern);
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                keys.addAll(scanResult.getResult());
                cursor = scanResult.getCursor();
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

            return new RedisResult<>(RedisResult.Type.SUCCESS, keys);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            return new RedisResult<>(RedisResult.Type.ERROR);
        }
    }

    public static void reconnect() {
        close();
        create(lastHost, lastUser, lastPassword, lastPort, lastDatabase, lastTimeout);
    }

    public static void close() {
        if (pool != null) {
            pool.close();
        }
    }

}