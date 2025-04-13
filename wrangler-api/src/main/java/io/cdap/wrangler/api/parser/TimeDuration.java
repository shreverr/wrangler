/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * A {@link Token} implementation representing time durations with units (ms, s,
 * m, h, d, w, M, y).
 */
@PublicEvolving
public class TimeDuration implements Token {
    private final String originalValue;
    private final long milliseconds;

    /**
     * Constructor that parses a string representing a time duration with unit.
     *
     * @param value The string value to parse (e.g., "5s", "10m", "24h", "7d")
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    public TimeDuration(String value) {
        this.originalValue = value;
        this.milliseconds = parseTimeDuration(value);
    }

    /**
     * Returns the duration in milliseconds.
     *
     * @return duration in milliseconds
     */
    public long getMilliseconds() {
        return milliseconds;
    }

    @Override
    public Object value() {
        return originalValue;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type().name());
        object.addProperty("value", originalValue);
        object.addProperty("milliseconds", milliseconds);
        return object;
    }

    private long parseTimeDuration(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Time duration value cannot be null or empty");
        }

        String trimmed = value.trim();
        String number = trimmed.replaceAll("[^\\d.]", "");
        String unit = trimmed.replaceAll("[\\d.]", "");

        try {
            long duration = Long.parseLong(number);
            switch (unit) {
                case "ms":
                    return duration;
                case "s":
                    return duration * 1000L;
                case "m":
                    return duration * 60L * 1000L;
                case "h":
                    return duration * 60L * 60L * 1000L;
                case "d":
                    return duration * 24L * 60L * 60L * 1000L;
                case "w":
                    return duration * 7L * 24L * 60L * 60L * 1000L;
                case "M": // Capital M for months to avoid confusion with minutes
                    return duration * 30L * 24L * 60L * 60L * 1000L; // Approximating month as 30 days
                case "y":
                    return duration * 365L * 24L * 60L * 60L * 1000L; // Approximating year as 365 days
                default:
                    throw new IllegalArgumentException("Invalid time unit: " + unit);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time duration number format: " + number);
        }
    }
}
