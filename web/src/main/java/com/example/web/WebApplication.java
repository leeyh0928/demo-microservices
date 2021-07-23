package com.example.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class WebApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(WebApplication.class, args);
		String redisHost = ctx.getEnvironment().getProperty("spring.redis.host");
		log.info("Connected to Redis: " + redisHost);
	}
}
