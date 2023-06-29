package com.onelity.bookme.config;

import com.onelity.bookme.service.BookingService;
import com.onelity.bookme.service.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.sql.DataSource;

@Configuration
public class PersistenceConfiguration {
    @Bean
    public DataSource dataSource() {
        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        builder.url("jdbc:postgresql://localhost:5432/Bookme-db?currentSchema=public&user=postgres&password=docker");
        System.out.println("My custom datasource bean has been initialized and set");
        return builder.build();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RoomService roomService() { return new RoomService();}

    @Bean
    public BookingService bookingService() { return new BookingService();}

//    @ControllerAdvice
//    public class GlobablExceptionHandler {
//
//        @ExceptionHandler(MyException.class)
//        public ResponseEntity<String> responseMyException(Exception e) {
//
//        }
//    }
}
