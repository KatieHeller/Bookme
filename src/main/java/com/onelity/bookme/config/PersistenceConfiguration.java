package com.onelity.bookme.config;

import javax.sql.DataSource;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.onelity.bookme.service.BookingService;
import com.onelity.bookme.service.RoomService;

/**
 * Configures the Postgres database and various services
 */
@Configuration
public class PersistenceConfiguration {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/Bookme-db?currentSchema=public&user=postgres&password=docker")
                .build();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RoomService roomService() {
        return new RoomService();
    }

    @Bean
    public BookingService bookingService() {
        return new BookingService();
    }

}
