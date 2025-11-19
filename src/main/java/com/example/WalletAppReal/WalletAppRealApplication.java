package com.example.WalletAppReal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WalletAppRealApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletAppRealApplication.class, args);
	}

}
