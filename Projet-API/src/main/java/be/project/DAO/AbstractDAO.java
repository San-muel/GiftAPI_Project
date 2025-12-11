package be.project.DAO;

import java.sql.Connection;

public abstract class AbstractDAO<T> extends DAO<T> {
    protected Connection connection;

    public AbstractDAO(Connection connection) {
        this.connection = connection;
    }
}
