package ua.nure.crowdchainnode.repository;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T, ID> {
    void save(T entity);
    void saveAll(Collection<T> entities);
    Optional<T> findById(ID id);
    Collection<T> findAll();

    void removeAll();
}
