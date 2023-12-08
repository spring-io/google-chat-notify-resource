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

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * A {@link Version} generated from the current date and time.
 *
 * @author Scott Frederick
 */
public final class TimestampVersion {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd.HHmmssnnnnnnnnn")
		.withZone(ZoneOffset.UTC);

	private TimestampVersion() {
	}

	public static Version now() {
		return new Version(FORMATTER.format(Clock.systemUTC().instant()));
	}

}
