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

package io.spring.concourse.googlechatnotify.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.concourse.googlechatnotify.command.payload.Metadata;
import io.spring.concourse.googlechatnotify.command.payload.OutRequest;
import io.spring.concourse.googlechatnotify.command.payload.OutResponse;
import io.spring.concourse.googlechatnotify.command.payload.Source;
import io.spring.concourse.googlechatnotify.command.payload.TimestampVersion;
import io.spring.concourse.googlechatnotify.webhook.OutgoingWebhook;
import io.spring.concourse.googlechatnotify.webhook.WebhookMessage;
import io.spring.concourse.googlechatnotify.webhook.WebhookResponse;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Delegate used to handle operations triggered from the {@link OutCommand}.
 *
 * @author Scott Frederick
 */
@Component
public class OutHandler {

	private final ConfigurableEnvironment environment;

	private final OutgoingWebhook webHook;

	private final ObjectMapper objectMapper;

	public OutHandler(ConfigurableEnvironment environment, OutgoingWebhook webHook, ObjectMapper objectMapper) {
        this.environment = environment;
        this.webHook = webHook;
		this.objectMapper = objectMapper;
	}

	public OutResponse handle(OutRequest request, String workingDir) {
		OutRequest.Params params = request.params();
		Assert.state(params.text() != null || params.cardFile() != null || params.textFile() != null,
				"At least one of 'text', 'card_file', or 'text_file' must be provided");
		Source source = request.source();
		WebhookMessage message = createWebhookMessage(params, workingDir);
		WebhookResponse response = this.webHook.send(source.getUrl(), message);
		List<Metadata> metadata = List.of(new Metadata("status", response.statusCode()),
				new Metadata("body", response.body()));
		return new OutResponse(TimestampVersion.now(), metadata);
	}

	private WebhookMessage createWebhookMessage(OutRequest.Params params, String workingDir) {
		WebhookMessage message = new WebhookMessage();
		if (params.text() != null) {
			message.put("text", params.text());
		}
		if (params.cardFile() != null) {
			message.put("cardsV2", readJsonFileContent(workingDir, params.cardFile()));
		}
		if (params.text() == null && params.cardFile() == null) {
			message.put("text", readFileContent(workingDir, params.textFile()));
		}
		else {
			addTextFileToEnvironment(workingDir, params.textFile());
		}
		return message;
	}

	private void addTextFileToEnvironment(String workingDir, String textFile) {
		if (StringUtils.hasText(textFile)) {
			String content = readFileContent(workingDir, textFile);
			PropertySource<?> propertySource = new MapPropertySource("textFile", Map.of("TEXT_FILE_CONTENT", content));
			this.environment.getPropertySources().addLast(propertySource);
		}
	}

	private List<Map<Object, Object>> readJsonFileContent(String workingDir, String fileName) {
		try {
			TypeReference<List<Map<Object, Object>>> typeRef = new TypeReference<>() {
			};
			return this.objectMapper.readValue(Paths.get(workingDir, fileName).toFile(), typeRef);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Error parsing JSON content from message file '" + fileName + "'", ex);
		}
	}

	private String readFileContent(String workingDir, String fileName) {
		try {
			return new String(Files.readAllBytes(Paths.get(workingDir, fileName)));
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Error reading file '" + fileName + "'", ex);
		}
	}

}
