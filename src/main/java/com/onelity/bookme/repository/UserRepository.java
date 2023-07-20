package com.onelity.bookme.repository;

import com.onelity.bookme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository which handles accessing users through the database, so they can be authenticated
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    public User findByUsername(String username);

}
