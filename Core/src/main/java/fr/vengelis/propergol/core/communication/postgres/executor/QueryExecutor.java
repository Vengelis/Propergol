package fr.vengelis.propergol.core.communication.postgres.executor;

import java.sql.Connection;

/**
 * SQL query executor with a result
 *
 * @param <T> Type of value desired
 */
public interface QueryExecutor<T> {
    /**
     * Perform a SQL query with a connection
     *
     * @param connection Connection JDBC
     * @return Instance of type desired obtained by the request
     * @throws Exception Exception during the request
     */
    T perform(Connection connection) throws Exception;
}