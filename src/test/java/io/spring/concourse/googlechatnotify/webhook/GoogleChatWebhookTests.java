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

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GoogleChatWebhook}.
 *
 * @author Scott Frederick
 */
@RestClientTest(value = GoogleChatWebhook.class,
		properties = { "ENV_KEY_1=value1", "ENV_KEY_2=value2", "ENV_KEY_QUOTED=env with \"quotes\"" })
class GoogleChatWebhookTests {

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private GoogleChatWebhook webhook;

	@Test
	void webhookCallSucceeds() {
		this.server.expect(requestTo("https://chat.example.com/"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.name").value("value"))
			.andRespond(withSuccess("success", MediaType.TEXT_PLAIN));
		WebhookResponse response = this.webhook.send("https://chat.example.com/",
				WebhookMessage.from(Map.of("name", "value")));
		assertThat(response.statusCode()).isEqualTo("200 OK");
		assertThat(response.body()).isEqualTo("success");
	}

	@Test
	void webhookCallWithJsonFieldSucceeds() {
		Map<String, Object> jsonValue = Map.of("key1", "value1", "key2", "value2");
		this.server.expect(requestTo("https://chat.example.com/"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.key1").value("value1"))
			.andExpect(jsonPath("$.key2").value("value2"))
			.andRespond(withSuccess("success", MediaType.TEXT_PLAIN));
		WebhookResponse response = this.webhook.send("https://chat.example.com/", WebhookMessage.from(jsonValue));
		assertThat(response.statusCode()).isEqualTo("200 OK");
		assertThat(response.body()).isEqualTo("success");
	}

	@Test
	void webhookCallWithEnvironmentVariablesSucceeds() {
		Map<String, Object> jsonValue = Map.of("key1", "${ENV_KEY_1}", "key2", "${ENV_KEY_2}");
		this.server.expect(requestTo("https://chat.example.com/"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.key1").value("value1"))
			.andExpect(jsonPath("$.key2").value("value2"))
			.andRespond(withSuccess("success", MediaType.TEXT_PLAIN));
		WebhookResponse response = this.webhook.send("https://chat.example.com/", WebhookMessage.from(jsonValue));
		assertThat(response.statusCode()).isEqualTo("200 OK");
		assertThat(response.body()).isEqualTo("success");
	}

	@Test
	void webhookCallWithQuotedJsonFieldSucceeds() {
		Map<String, Object> jsonValue = Map.of("key1", "value with \"quotes\"", "key2", "${ENV_KEY_QUOTED}");
		this.server.expect(requestTo("https://chat.example.com/"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.key1").value("value with \"quotes\""))
			.andExpect(jsonPath("$.key2").value("env with \"quotes\""))
			.andRespond(withSuccess("success", MediaType.TEXT_PLAIN));
		WebhookResponse response = this.webhook.send("https://chat.example.com/", WebhookMessage.from(jsonValue));
		assertThat(response.statusCode()).isEqualTo("200 OK");
		assertThat(response.body()).isEqualTo("success");
	}

	@Test
	void webhookFails() {
		this.server.expect(requestTo("https://chat.example.com/"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withBadRequest());
		WebhookResponse response = this.webhook.send("https://chat.example.com/",
				WebhookMessage.from(Map.of("test", "test")));
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
		assertThat(response.body()).isEmpty();
	}

}
