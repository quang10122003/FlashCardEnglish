package com.TestFlashCard.FlashCard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.TestFlashCard.FlashCard")
@EnableCaching
public class FlashCardApplication {
	public static void main(String[] args) {
		SpringApplication.run(FlashCardApplication.class, args);
	}
}
