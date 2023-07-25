package com.onelity.bookme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The Bookme application allows users to book the meeting rooms of Onelity offices.
 */
@SpringBootApplication
// @EntityScan(basePackages = {"com.bookme.model"} )
// @EnableJpaRepositories(basePackages = {"com.bookme.repository"})
public class BookmeApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(BookmeApplication.class, args);
    }

}
