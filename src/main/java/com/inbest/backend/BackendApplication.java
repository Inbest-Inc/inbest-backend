package com.inbest.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// .env dosyasını yükler
		Dotenv dotenv = Dotenv.load();

		// Environment değişkenlerini sistem özelliklerine ekler
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST"));
		System.setProperty("POSTGRES_PORT", dotenv.get("POSTGRES_PORT"));
		System.setProperty("POSTGRES_DB", dotenv.get("POSTGRES_DB"));
		System.setProperty("POSTGRES_USER", dotenv.get("POSTGRES_USER"));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD"));
		System.setProperty("USER_EMAIL", dotenv.get("USER_EMAIL"));
		System.setProperty("EMAIL_PASSWORD", dotenv.get("EMAIL_PASSWORD"));
		System.setProperty("BUCKET_NAME", dotenv.get("BUCKET_NAME"));
		System.setProperty("BUCKET_ACCESS_KEY", dotenv.get("BUCKET_ACCESS_KEY"));
		System.setProperty("BUCKET_SECRET_KEY", dotenv.get("BUCKET_SECRET_KEY"));

		// Spring Boot uygulamasını başlatır
		SpringApplication.run(BackendApplication.class, args);
	}
}
