package com.salaboy.payments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication

public class PaymentsServiceApplication {



	public static void main(String[] args) {
		SpringApplication.run(PaymentsServiceApplication.class, args);
	}



}

@RestController
@Slf4j
@RequestMapping("api")
class PaymentsRestController{

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

	@GetMapping("/{reservationId}")
	public boolean authorized(@PathVariable String reservationId){
		log.info("Querying for reservationId: "+reservationId );
		return paymentRequests.get(reservationId);
	}
}

@Controller
@Slf4j
class PaymentsSiteController {


	@Value("${version:0.0.0}")
	private String version;


	@Autowired
	private PaymentsRestController paymentsRestController;



	@GetMapping("/")
	public String index(@RequestParam(value = "sessionId", required = true) String sessionId,
						@RequestParam(value = "reservationId", required = true) String reservationId,
						Model model) {

		model.addAttribute("version", version);
		model.addAttribute("sessionId", sessionId);
		model.addAttribute("reservationId", reservationId);
		model.addAttribute("status", paymentsRestController.authorized(reservationId));



		return "index";
	}
}
