package com.onelity.bookme.service;

import com.onelity.bookme.model.CustomUserDetails;
import com.onelity.bookme.model.User;
import com.onelity.bookme.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Implementation of CustomUserDetailsService which contains custom loadUserByUsername method */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  @Autowired private UserRepository repository;

  /**
   * Obtains User object from "users" database table based on their username
   *
   * @param username username of user desired
   * @return returns User object if user is present
   * @throws UsernameNotFoundException exception if no user with given username exists
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = repository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    CustomUserDetails customUserDetails = new CustomUserDetails();
    customUserDetails.setUser(user);
    Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    if (user.getRole().equals("ROLE_ADMIN")) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    customUserDetails.setAuthorities(authorities);
    return customUserDetails;
  }
}
