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
 * A {@link Token} implementation representing byte sizes with units (B, KB, MB,
 * GB, TB, PB).
 */
@PublicEvolving
public class ByteSize implements Token {
    private final String originalValue;
    private final long bytes;

    /**
     * Constructor that parses a string representing a byte size with unit.
     *
     * @param value The string value to parse (e.g., "10KB", "5MB", "2GB")
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    public ByteSize(String value) {
        this.originalValue = value;
        this.bytes = parseByteSize(value);
    }

    /**
     * Returns the size in bytes.
     *
     * @return size in bytes
     */
    public long getBytes() {
        return bytes;
    }

    @Override
    public Object value() {
        return originalValue;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type().name());
        object.addProperty("value", originalValue);
        object.addProperty("bytes", bytes);
        return object;
    }

    private long parseByteSize(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Byte size value cannot be null or empty");
        }

        String trimmed = value.trim().toUpperCase();
        String number = trimmed.replaceAll("[^\\d.]", "");
        String unit = trimmed.replaceAll("[\\d.]", "");

        try {
            long size = Long.parseLong(number);
            switch (unit) {
                case "B":
                    return size;
                case "KB":
                    return size * 1024L;
                case "MB":
                    return size * 1024L * 1024L;
                case "GB":
                    return size * 1024L * 1024L * 1024L;
                case "TB":
                    return size * 1024L * 1024L * 1024L * 1024L;
                case "PB":
                    return size * 1024L * 1024L * 1024L * 1024L * 1024L;
                default:
                    throw new IllegalArgumentException("Invalid byte unit: " + unit);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid byte size number format: " + number);
        }
    }
}

