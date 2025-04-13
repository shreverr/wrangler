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

package io.cdap.directives.aggregates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling byte size values and conversions.
 */
public class ByteSize {
    private static final Pattern SIZE_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)(B|KB|MB|GB|TB)$");
    private static final long BYTES_IN_KB = 1024L;
    private static final long BYTES_IN_MB = BYTES_IN_KB * 1024L;
    private static final long BYTES_IN_GB = BYTES_IN_MB * 1024L;
    private static final long BYTES_IN_TB = BYTES_IN_GB * 1024L;

    private final long bytes;

    private ByteSize(long bytes) {
        this.bytes = bytes;
    }

    /**
     * Parses a string representation of byte size (e.g., "1.5MB", "500KB", "2GB")
     * 
     * @param value The string to parse
     * @return A ByteSize instance
     * @throws IllegalArgumentException if the string is not a valid byte size
     */
    public static ByteSize parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        Matcher matcher = SIZE_PATTERN.matcher(value.trim().toUpperCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: " + value);
        }

        double number = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2);

        long bytes;
        switch (unit) {
            case "B":
                bytes = (long) number;
                break;
            case "KB":
                bytes = (long) (number * BYTES_IN_KB);
                break;
            case "MB":
                bytes = (long) (number * BYTES_IN_MB);
                break;
            case "GB":
                bytes = (long) (number * BYTES_IN_GB);
                break;
            case "TB":
                bytes = (long) (number * BYTES_IN_TB);
                break;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }

        return new ByteSize(bytes);
    }

    /**
     * Creates a ByteSize instance from a number of bytes
     */
    public static ByteSize fromBytes(long bytes) {
        return new ByteSize(bytes);
    }

    public long toBytes() {
        return bytes;
    }

    public double toKilobytes() {
        return (double) bytes / BYTES_IN_KB;
    }

    public double toMegabytes() {
        return (double) bytes / BYTES_IN_MB;
    }

    public double toGigabytes() {
        return (double) bytes / BYTES_IN_GB;
    }

    public double toTerabytes() {
        return (double) bytes / BYTES_IN_TB;
    }
}