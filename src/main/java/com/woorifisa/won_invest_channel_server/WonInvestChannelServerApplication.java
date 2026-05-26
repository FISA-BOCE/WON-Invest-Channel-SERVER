package com.woorifisa.won_invest_channel_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WonInvestChannelServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WonInvestChannelServerApplication.class, args);
	}

}
