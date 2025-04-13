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

import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {

    @Test
    public void testParse() {
        // Test basic units
        Assert.assertEquals(1_000_000L, TimeDuration.parse("1ms").toNanos());
        Assert.assertEquals(1_000_000_000L, TimeDuration.parse("1s").toNanos());
        Assert.assertEquals(60_000_000_000L, TimeDuration.parse("1m").toNanos());
        Assert.assertEquals(3600_000_000_000L, TimeDuration.parse("1h").toNanos());
        Assert.assertEquals(86400_000_000_000L, TimeDuration.parse("1d").toNanos());

        // Test decimal values
        Assert.assertEquals(1_500_000L, TimeDuration.parse("1.5ms").toNanos());
        Assert.assertEquals(1_500_000_000L, TimeDuration.parse("1.5s").toNanos());

        // Test case insensitivity
        Assert.assertEquals(1_000_000_000L, TimeDuration.parse("1S").toNanos());
        Assert.assertEquals(1_000_000_000L, TimeDuration.parse("1s").toNanos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidFormat() {
        TimeDuration.parse("1.5"); // Missing unit
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidUnit() {
        TimeDuration.parse("1.5w"); // Invalid unit
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() {
        TimeDuration.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmpty() {
        TimeDuration.parse("");
    }

    @Test
    public void testConversions() {
        TimeDuration duration = TimeDuration.fromNanos(60_000_000_000L); // 1 minute in nanos

        Assert.assertEquals(60_000_000_000L, duration.toNanos());
        Assert.assertEquals(60_000.0, duration.toMillis(), 0.001);
        Assert.assertEquals(60.0, duration.toSeconds(), 0.001);
        Assert.assertEquals(1.0, duration.toMinutes(), 0.001);
        Assert.assertEquals(0.016666667, duration.toHours(), 0.000001);
        Assert.assertEquals(0.000694444, duration.toDays(), 0.000001);
    }
}