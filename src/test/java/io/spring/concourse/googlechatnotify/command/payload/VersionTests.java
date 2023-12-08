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
 * Tests for {@link Version}.
 *
 * @author Phillip Webb
 * @author Scott Frederick
 */
@JsonTest
class VersionTests {

	@Autowired
	private JacksonTester<Version> json;

	@Test
	void createWhenBuildNumberIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Version(""))
			.withMessage("Build Number must not be empty");
	}

	@Test
	void writeSerializesJson() throws Exception {
		Version version = new Version("5678");
		assertThat(this.json.write(version)).isEqualTo("version.json");
	}

	@Test
	void readDeserializesJson() throws Exception {
		Version version = this.json.readObject("version.json");
		assertThat(version.getBuildNumber()).isEqualTo("5678");
	}

}
