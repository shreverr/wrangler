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
 * Utility class for handling time duration values and conversions.
 */
public class TimeDuration {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)(ms|s|m|h|d)$");
    private static final long NANOS_IN_MILLI = 1_000_000L;
    private static final long NANOS_IN_SECOND = NANOS_IN_MILLI * 1000L;
    private static final long NANOS_IN_MINUTE = NANOS_IN_SECOND * 60L;
    private static final long NANOS_IN_HOUR = NANOS_IN_MINUTE * 60L;
    private static final long NANOS_IN_DAY = NANOS_IN_HOUR * 24L;

    private final long nanos;

    private TimeDuration(long nanos) {
        this.nanos = nanos;
    }

    /**
     * Parses a string representation of time duration (e.g., "1.5s", "500ms", "2h")
     * 
     * @param value The string to parse
     * @return A TimeDuration instance
     * @throws IllegalArgumentException if the string is not a valid time duration
     */
    public static TimeDuration parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        Matcher matcher = TIME_PATTERN.matcher(value.trim().toLowerCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: " + value);
        }

        double number = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2);

        long nanos;
        switch (unit) {
            case "ms":
                nanos = (long) (number * NANOS_IN_MILLI);
                break;
            case "s":
                nanos = (long) (number * NANOS_IN_SECOND);
                break;
            case "m":
                nanos = (long) (number * NANOS_IN_MINUTE);
                break;
            case "h":
                nanos = (long) (number * NANOS_IN_HOUR);
                break;
            case "d":
                nanos = (long) (number * NANOS_IN_DAY);
                break;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }

        return new TimeDuration(nanos);
    }

    /**
     * Creates a TimeDuration instance from a number of nanoseconds
     */
    public static TimeDuration fromNanos(long nanos) {
        return new TimeDuration(nanos);
    }

    public long toNanos() {
        return nanos;
    }

    public double toMillis() {
        return (double) nanos / NANOS_IN_MILLI;
    }

    public double toSeconds() {
        return (double) nanos / NANOS_IN_SECOND;
    }

    public double toMinutes() {
        return (double) nanos / NANOS_IN_MINUTE;
    }

    public double toHours() {
        return (double) nanos / NANOS_IN_HOUR;
    }

    public double toDays() {
        return (double) nanos / NANOS_IN_DAY;
    }
}