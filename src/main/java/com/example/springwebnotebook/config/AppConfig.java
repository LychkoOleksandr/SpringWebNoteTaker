package com.example.springwebnotebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {

    @Bean
    public String singletonBean() {
        return "singleton bean";
    }

    @Bean
    @Scope("prototype")
    public String prototypeBean() {
        return "prototype bean";
    }
}