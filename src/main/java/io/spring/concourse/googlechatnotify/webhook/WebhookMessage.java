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

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;

/**
 * A response from an outgoing webhook call.
 *
 * @author Scott Frederick
 */
public class WebhookMessage extends HashMap<String, Object> {

	public static WebhookMessage from(Map<String, Object> map) {
		WebhookMessage message = new WebhookMessage();
		message.putAll(map);
		return message;
	}

	void resolvePlaceholders(Environment environment) {
		resolvePlaceholders(this, environment);
	}

	@SuppressWarnings("unchecked")
	private void resolvePlaceholders(Map<String, Object> map, Environment environment) {
		for (Entry<String, Object> entry : map.entrySet()) {
			if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
				resolvePlaceholders((Map<String, Object>) entry.getValue(), environment);
			}
			if (entry.getValue().getClass().isAssignableFrom(String.class)) {
				map.replace(entry.getKey(), environment.resolvePlaceholders(entry.getValue().toString()));
			}
		}

	}

}
