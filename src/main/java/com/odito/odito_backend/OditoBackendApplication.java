package com.odito.odito_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.odito.odito_backend",
	"com.squelette.squelette_backend"
})
public class OditoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OditoBackendApplication.class, args);
	}

}
