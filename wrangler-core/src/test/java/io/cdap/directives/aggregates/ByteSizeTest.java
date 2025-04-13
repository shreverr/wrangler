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

public class ByteSizeTest {

    @Test
    public void testParse() {
        // Test basic units
        Assert.assertEquals(1024L, ByteSize.parse("1KB").toBytes());
        Assert.assertEquals(1048576L, ByteSize.parse("1MB").toBytes());
        Assert.assertEquals(1073741824L, ByteSize.parse("1GB").toBytes());
        Assert.assertEquals(1099511627776L, ByteSize.parse("1TB").toBytes());

        // Test decimal values
        Assert.assertEquals(1536L, ByteSize.parse("1.5KB").toBytes());
        Assert.assertEquals(1572864L, ByteSize.parse("1.5MB").toBytes());

        // Test case insensitivity
        Assert.assertEquals(1024L, ByteSize.parse("1Kb").toBytes());
        Assert.assertEquals(1024L, ByteSize.parse("1kb").toBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidFormat() {
        ByteSize.parse("1.5"); // Missing unit
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidUnit() {
        ByteSize.parse("1.5PB"); // Invalid unit
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() {
        ByteSize.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmpty() {
        ByteSize.parse("");
    }

    @Test
    public void testConversions() {
        ByteSize size = ByteSize.fromBytes(1024L * 1024L); // 1MB in bytes

        Assert.assertEquals(1024L * 1024L, size.toBytes());
        Assert.assertEquals(1024.0, size.toKilobytes(), 0.001);
        Assert.assertEquals(1.0, size.toMegabytes(), 0.001);
        Assert.assertEquals(0.000976562, size.toGigabytes(), 0.000001);
        Assert.assertEquals(0.000000954, size.toTerabytes(), 0.000001);
    }
}