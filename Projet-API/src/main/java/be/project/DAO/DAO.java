package be.project.DAO;

import java.util.List;

public abstract class DAO<T> {
    public abstract boolean create(T obj);
    public abstract boolean delete(T obj);
    public abstract boolean update(T obj);
    public abstract T find(int id);
    public abstract List<T> findAll();
}