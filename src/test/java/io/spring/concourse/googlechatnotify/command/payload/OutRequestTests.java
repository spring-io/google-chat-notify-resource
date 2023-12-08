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

package io.spring.concourse.googlechatnotify.command.payload;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link OutRequest}.
 *
 * @author Scott Frederick
 */
@JsonTest
class OutRequestTests {

	private final Source source = new Source("https://chat.example.com");

	@Autowired
	private JacksonTester<OutRequest> json;

	@Test
	void createWhenSourceIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OutRequest(null, null))
			.withMessage("Source must not be null");
	}

	@Test
	void createWhenParamsIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OutRequest(this.source, null))
			.withMessage("Params must not be null");
	}

	@Test
	void readDeserializesJson() throws Exception {
		OutRequest request = this.json.readObject("out-request.json");
		assertThat(request.source().getUrl()).isEqualTo("https://chat.example.com");
		assertThat(request.params().text()).isEqualTo("sample text");
		assertThat(request.params().cardFile()).isEqualTo("card.json");
		assertThat(request.params().textFile()).isEqualTo("test-file.txt");
	}

}
