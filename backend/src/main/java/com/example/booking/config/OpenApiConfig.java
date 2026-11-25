package com.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI roomBookingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Room Booking API")
                .description("Meeting room booking system: rooms, bookings, availability and schedules")
                .version("v1"));
    }
}