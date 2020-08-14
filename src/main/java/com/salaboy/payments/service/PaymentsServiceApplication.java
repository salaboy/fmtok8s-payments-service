package com.salaboy.payments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@Slf4j
public class PaymentsServiceApplication {



	private ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) {
		SpringApplication.run(PaymentsServiceApplication.class, args);
	}

	private Map<String, Boolean> paymentRequests = new ConcurrentHashMap<>();


	@PostMapping("/")
	public String pay(@RequestBody Payment payment) throws InterruptedException, JsonProcessingException {
		paymentRequests.put(payment.getReservationId(), false);
		new Thread("payments-processor") {
			public void run() {


				for (int i = 0; i < 10; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					log.info("> processing payment: " + payment.getReservationId() + " with amount: " + payment.getAmount());
				}

				paymentRequests.put(payment.getReservationId(), true);

			}
		}.start();

		return "OK!";
	}

	@GetMapping("/{paymentId}")
	public boolean authorized(@PathVariable String paymentId){
		return paymentRequests.get(paymentId);
	}

}
