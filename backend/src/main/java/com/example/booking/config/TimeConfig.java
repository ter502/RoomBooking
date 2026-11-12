package com.example.booking.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    public static final ZoneId ZONE = ZoneId.of("Asia/Taipei");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }
}