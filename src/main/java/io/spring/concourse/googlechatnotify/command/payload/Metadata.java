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

import java.util.Objects;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A metadata item that can be returned as part of an {@link OutResponse}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Scott Frederick
 */
public class Metadata {

	private final String name;

	private final Object value;

	public Metadata(String name, Object value) {
		Assert.hasText(name, "Name must not be empty");
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Metadata other = (Metadata) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.value);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("name", this.name).append("value", this.value).toString();
	}

}
