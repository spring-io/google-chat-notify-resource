/*
 * Copyright 2017-2023 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.concourse.googlechatnotify.command.payload.Metadata;
import io.spring.concourse.googlechatnotify.command.payload.OutRequest;
import io.spring.concourse.googlechatnotify.command.payload.OutResponse;
import io.spring.concourse.googlechatnotify.command.payload.Source;
import io.spring.concourse.googlechatnotify.webhook.OutgoingWebhook;
import io.spring.concourse.googlechatnotify.webhook.WebhookMessage;
import io.spring.concourse.googlechatnotify.webhook.WebhookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link OutHandler}.
 *
 * @author Scott Frederick
 */
@ExtendWith(MockitoExtension.class)
class OutHandlerTests {

	@TempDir
	private File tempDir;

	private MockEnvironment environment;

	@Mock
	private OutgoingWebhook webHook;

	private OutHandler handler;

	@BeforeEach
	void setup() {
		this.environment = new MockEnvironment();
		this.handler = new OutHandler(this.environment, this.webHook, new ObjectMapper());
	}

	@Test
	void handleWithNoParamsFails() {
		OutRequest request = createRequest(null, null, null);
		assertThatIllegalStateException().isThrownBy(() -> this.handler.handle(request, ""))
				.withMessageContaining("At least one of 'text', 'card_file', or 'text_file' must be provided");
	}

	@Test
	void handleWithTextSendsToWebHook() throws IOException {
		File textFile = createFile("info.txt", "text from file");
		OutRequest request = createRequest("sample text", null, textFile.getAbsolutePath());
		given(this.webHook.send("https://chat.example.com", WebhookMessage.from(Map.of("text", "sample text"))))
			.willReturn(new WebhookResponse("200 OK", "test response"));
		OutResponse response = this.handler.handle(request, "");
		int year = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR);
		assertThat(response.version().getBuildNumber()).startsWith(String.valueOf(year));
		assertThat(response.metadata()).containsExactly(new Metadata("status", "200 OK"),
				new Metadata("body", "test response"));
		assertThat(this.environment.getProperty("TEXT_FILE_CONTENT")).isEqualTo("text from file");
	}

	@Test
	void handleWithCardJsonFileSendsToWebHook() throws IOException {
		File textFile = createFile("info.txt", "text from file");
		String cardJson = """
				[{ "key1": "value1", "key2": 2, "key3": { "subkey1": "subvalue1", "subkey2": "subvalue2" } }]
				""".strip();
		List<Map<String, Object>> cardJsonMap = List
			.of(Map.of("key1", "value1", "key2", 2, "key3", Map.of("subkey1", "subvalue1", "subkey2", "subvalue2")));
		File jsonFile = createFile("card_json_file", cardJson);
		OutRequest request = createRequest(null, jsonFile.getAbsolutePath(), textFile.getAbsolutePath());
		given(this.webHook.send("https://chat.example.com", WebhookMessage.from(Map.of("cardsV2", cardJsonMap))))
			.willReturn(new WebhookResponse("200 OK", "test response"));
		OutResponse response = this.handler.handle(request, "");
		assertThat(response.metadata()).containsExactly(new Metadata("status", "200 OK"),
				new Metadata("body", "test response"));
		assertThat(this.environment.getProperty("TEXT_FILE_CONTENT")).isEqualTo("text from file");
	}

	@Test
	void handleWithTextFileOnlySendsToWebhook() throws IOException {
		File textFile = createFile("info.txt", "text from file");
		OutRequest request = createRequest(null, null, textFile.getAbsolutePath());
		given(this.webHook.send("https://chat.example.com", WebhookMessage.from(Map.of("text", "text from file"))))
				.willReturn(new WebhookResponse("200 OK", "test response"));
		OutResponse response = this.handler.handle(request, "");
		assertThat(response.metadata()).containsExactly(new Metadata("status", "200 OK"),
				new Metadata("body", "test response"));
		assertThat(this.environment.getProperty("TEXT_FILE_CONTENT")).isNull();
	}

	private File createFile(String name, String content) throws IOException {
		File file = new File(this.tempDir, name);
		FileCopyUtils.copy(content.getBytes(), file);
		return file;
	}

	private OutRequest createRequest(String text, String cardFile, String textFile) {
		return new OutRequest(new Source("https://chat.example.com"), new OutRequest.Params(text, cardFile, textFile));
	}

}
