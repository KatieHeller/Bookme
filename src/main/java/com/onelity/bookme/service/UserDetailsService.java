package com.onelity.bookme.service;

import com.onelity.bookme.model.User;
import com.onelity.bookme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of UserDetailsService which contains custom loadUserByUsername method
 */
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository repository;

    /**
     * Obtains User object from "users" database table based on their username
     * @param username username of user desired
     * @return returns User object if user is present
     * @throws UsernameNotFoundException exception if no user with given username exists
     */
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return user;
    }

}
