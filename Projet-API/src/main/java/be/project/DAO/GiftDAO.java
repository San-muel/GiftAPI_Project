package be.project.DAO;

import java.sql.Connection;
import java.util.List;

import be.project.model.Gift;

public class GiftDAO extends AbstractDAO<Gift> { 

    public GiftDAO(Connection connect) {
        super(connect); 
    }

    @Override
    public boolean create(Gift obj) {
        // À implémenter plus tard
        return false;
    }

    @Override
    public boolean delete(Gift obj) {
        return false;
    }

    @Override
    public boolean update(Gift obj) {
        return false;
    }

    @Override
    public Gift find(int id) {
        return null;
    }

    @Override
    public List<Gift> findAll() {
        return null;
    }
}