/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.concourse.googlechatnotify.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.concourse.googlechatnotify.system.ConsoleLogger;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Sends messages to a Google Chat webhook.
 *
 * @author Scott Frederick
 */
@Component
public class GoogleChatWebhook implements OutgoingWebhook {

	private final Environment environment;

	private final RestClient restClient;

	private final ObjectMapper objectMapper;

	private static final ConsoleLogger console = new ConsoleLogger();

	public GoogleChatWebhook(Environment environment, RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
		this.environment = environment;
		this.restClient = restClientBuilder.build();
		this.objectMapper = objectMapper;
	}

	public WebhookResponse send(String url, WebhookMessage message) {
		try {
			String messageString = this.objectMapper.writeValueAsString(message);
			String resolved = this.environment.resolvePlaceholders(messageString);
			console.log("Sending message '" + resolved + "' to webhook");
			String body = this.restClient.post()
				.uri(url)
				.header("accept", "application/json; charset=UTF-8")
				.body(resolved)
				.retrieve()
				.body(String.class);
			return new WebhookResponse(HttpStatus.OK.toString(), body);
		}
		catch (RestClientResponseException rce) {
			console.log("Error sending request: " + rce.getMessage());
			return new WebhookResponse(rce.getStatusText(), rce.getResponseBodyAsString());
		}
		catch (JsonProcessingException jpe) {
			console.log("Error formatting message for sending: " + jpe.getMessage());
			throw new IllegalStateException("Error formatting message for sending", jpe);
		}
	}

}
