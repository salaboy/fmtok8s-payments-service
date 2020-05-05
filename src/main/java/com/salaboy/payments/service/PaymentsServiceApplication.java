package com.salaboy.payments.service;

import com.salaboy.cloudevents.helper.CloudEventsHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.json.Json;
import io.cloudevents.v03.AttributesImpl;
import io.cloudevents.v03.CloudEventBuilder;
import io.zeebe.cloudevents.ZeebeCloudEventsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

@SpringBootApplication
@RestController
@Slf4j
public class PaymentsServiceApplication {

	@Value("${CLOUD_EVENTS_BRIDGE:http://localhost:8080}")
	private String CLOUD_EVENTS_BRIDGE;

	public static void main(String[] args) {
		SpringApplication.run(PaymentsServiceApplication.class, args);
	}

	@PostMapping("/")
	public String pay(@RequestBody Payment payment) throws InterruptedException {

		CloudEventBuilder<String> cloudEventBuilder = CloudEventBuilder.<String>builder()
				.withId(UUID.randomUUID().toString())
				.withTime(ZonedDateTime.now())
				.withType("Payments.Received")
				.withSource(URI.create("payments.service.default"))
				.withData(Json.encode(payment))
				.withDatacontenttype("application/json")
				.withSubject("Payment Service");
		String[] subjectSplit = payment.getSubject().split(":");
		CloudEvent<AttributesImpl, String> paymentReceivedZeebeCloudEvent = ZeebeCloudEventsHelper.buildZeebeCloudEvent(cloudEventBuilder)
				.withWorkflowKey(subjectSplit[0])
				.withWorkflowInstanceKey(subjectSplit[1])
				.withJobKey(subjectSplit[2])
				.build();

		String cloudEventJson = Json.encode(paymentReceivedZeebeCloudEvent);
		log.info("Before sending Cloud Event: " + cloudEventJson);
		WebClient webClient = WebClient.builder().baseUrl(CLOUD_EVENTS_BRIDGE).filter(logRequest()).build();

		WebClient.ResponseSpec postCloudEvent = CloudEventsHelper.createPostCloudEvent(webClient, "/", paymentReceivedZeebeCloudEvent);

		postCloudEvent.bodyToMono(String.class).doOnError(t -> t.printStackTrace())
				.doOnSuccess(s -> log.info("Result -> " + s)).subscribe();


		new Thread("payments-processor") {
			public void run() {
				for (int i = 0; i < 10; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					log.info("> processing payment: " + Json.encode(payment));
				}


				CloudEventBuilder<String> cloudEventBuilder = CloudEventBuilder.<String>builder()
						.withId(UUID.randomUUID().toString())
						.withTime(ZonedDateTime.now())
						.withType("Payments.Approved")
						.withSource(URI.create("payments.service.default"))
						.withData(Json.encode(payment))
						.withDatacontenttype("application/json")
						.withSubject("payments.service.default");

				CloudEvent<AttributesImpl, String> zeebeCloudEvent = ZeebeCloudEventsHelper
																	.buildZeebeCloudEvent(cloudEventBuilder)
																	.withCorrelationKey(payment.getPaymentId())
																	.build();

				String paymentApprovedCloudEventJson = Json.encode(zeebeCloudEvent);
				log.info("Before sending Payment Approved Cloud Event: " + paymentApprovedCloudEventJson);
				WebClient webClientApproved = WebClient.builder().baseUrl(CLOUD_EVENTS_BRIDGE).filter(logRequest()).build();

				WebClient.ResponseSpec postApprovedCloudEvent = CloudEventsHelper.createPostCloudEvent(webClientApproved, "/message", zeebeCloudEvent);

				postApprovedCloudEvent.bodyToMono(String.class).doOnError(t -> t.printStackTrace())
						.doOnSuccess(s -> log.info("Result -> " + s)).subscribe();
			}
		}.start();




		return "OK!";
	}


	private static ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			log.info("Request: " + clientRequest.method() + " - " + clientRequest.url());
			clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info(name + "=" + value)));
			return Mono.just(clientRequest);
		});
	}

}
