package kqlhotel.dao;

import java.util.List;

public interface DAO_Interface<T> {
    List<T> getAll();
    T getById(String id);
    boolean create(T t);
    boolean update(T t);
    boolean delete(String id);
}
