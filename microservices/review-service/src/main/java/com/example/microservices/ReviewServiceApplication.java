package com.example.microservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class ReviewServiceApplication {
	private final int connectionPoolSize;

	public ReviewServiceApplication(
			@Value("${spring.datasource.maximum-pool-size:10}")
			int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
	}

	@Bean
	public Scheduler jdbcScheduler() {
		log.info("Creates a jdbcScheduler with connectionPoolSize = " + connectionPoolSize);
		return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);
		String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
		log.info("Connected to MySQL: " + mysqlUri);
	}
}
