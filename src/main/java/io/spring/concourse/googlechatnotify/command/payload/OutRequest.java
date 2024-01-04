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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Request to the {@code "/opt/resource/out"} script.
 *
 * @author Scott Frederick
 * @param source the source configuration
 * @param params configuration parameters
 */
public record OutRequest(Source source, Params params) {

	public OutRequest(@JsonProperty("source") Source source, @JsonProperty("params") Params params) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(params, "Params must not be null");
		this.source = source;
		this.params = params;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("source", this.source).append("params", this.params).toString();
	}

	/**
	 * Parameters for the {@link OutRequest}.
	 *
	 * @param text formatted text to send to the webhook
	 * @param cardFile JSON card content to send to the webhook
	 * @param textFile a text file that can be included in webhook content
	 */
	public record Params(String text, String cardFile, String textFile) {

		@JsonCreator
		public Params(@JsonProperty("text") String text, @JsonProperty("card_file") String cardFile,
				@JsonProperty("text_file") String textFile) {
			this.text = text;
			this.cardFile = cardFile;
			this.textFile = textFile;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("text", this.text)
				.append("cardFile", this.cardFile)
				.append("textFile", this.textFile)
				.toString();
		}

	}

}
