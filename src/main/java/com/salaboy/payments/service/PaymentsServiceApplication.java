package com.salaboy.payments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaboy.cloudevents.helper.CloudEventsHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import io.zeebe.cloudevents.ZeebeCloudEventsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

	@Value("${ZEEBE_CLOUD_EVENTS_ROUTER:http://localhost:8080}")
	private String ZEEBE_CLOUD_EVENTS_ROUTER;

	private ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) {
		SpringApplication.run(PaymentsServiceApplication.class, args);
	}

	private void logCloudEvent(CloudEvent cloudEvent) {
		EventFormat format = EventFormatProvider
				.getInstance()
				.resolveFormat(JsonFormat.CONTENT_TYPE);

		log.info("Cloud Event: " + new String(format.serialize(cloudEvent)));

	}

	@PostMapping("/")
	public String pay(@RequestBody Payment payment) throws InterruptedException, JsonProcessingException {
		String paymentString = objectMapper.writeValueAsString(payment);
		String doubleQuotedPaymentString = objectMapper.writeValueAsString(paymentString);
		CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v03()
				.withId(UUID.randomUUID().toString())
				.withTime(ZonedDateTime.now())
				.withType("Payments.RequestReceived")
				.withSource(URI.create("payments.service.default"))
				.withData(doubleQuotedPaymentString.getBytes())
				.withDataContentType("application/json")
				.withSubject("Payment Service");
		String[] subjectSplit = payment.getSubject().split(":");
		CloudEvent paymentReceivedZeebeCloudEvent = ZeebeCloudEventsHelper.buildZeebeCloudEvent(cloudEventBuilder)
				.withWorkflowKey(subjectSplit[0])
				.withWorkflowInstanceKey(subjectSplit[1])
				.withJobKey(subjectSplit[2])
				.build();


		logCloudEvent(paymentReceivedZeebeCloudEvent);
		WebClient webClient = WebClient.builder().baseUrl(ZEEBE_CLOUD_EVENTS_ROUTER).filter(logRequest()).build();

		WebClient.ResponseSpec postCloudEvent = CloudEventsHelper.createPostCloudEvent(webClient, "/", paymentReceivedZeebeCloudEvent);

		postCloudEvent.bodyToMono(String.class).doOnError(t -> t.printStackTrace())
				.doOnSuccess(s -> log.info("Result -> " + s)).subscribe();


		new Thread("payments-processor") {
			public void run() {

				String doubleQuotedPaymentString = null;
				try {
					String paymentString = objectMapper.writeValueAsString(payment);
					doubleQuotedPaymentString = objectMapper.writeValueAsString(paymentString);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < 10; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					log.info("> processing payment: " + doubleQuotedPaymentString);
				}


				CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v03()
						.withId(UUID.randomUUID().toString())
						.withTime(ZonedDateTime.now())
						.withType("Payments.Authorized")
						.withSource(URI.create("payments.service.default"))
						.withData(doubleQuotedPaymentString.getBytes())
						.withDataContentType("application/json")
						.withSubject("payments.service.default");

				CloudEvent zeebeCloudEvent = ZeebeCloudEventsHelper
																	.buildZeebeCloudEvent(cloudEventBuilder)
																	.withCorrelationKey(payment.getPaymentId())
																	.build();

				logCloudEvent(zeebeCloudEvent);

				WebClient webClientApproved = WebClient.builder().baseUrl(ZEEBE_CLOUD_EVENTS_ROUTER).filter(logRequest()).build();

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
