package com.onelity.bookme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.bookme.model"} )
@EnableJpaRepositories(basePackages = {"com.bookme.repository"})
public class BookmeApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(BookmeApplication.class, args);
	}

}
