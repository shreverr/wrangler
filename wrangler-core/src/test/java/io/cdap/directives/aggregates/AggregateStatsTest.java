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

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsTest {

    @Test
    public void testTotalAggregation() throws Exception {
        String[] directives = new String[] {
                "aggregate-stats :size :time total_size_mb total_time_s MB s"
        };

        List<Row> rows = Arrays.asList(
                createRow("100MB", "30s"),
                createRow("200MB", "45s"),
                createRow("300MB", "60s"));

        List<Row> results = TestingRig.execute(directives, rows);

        // Check that all input rows are preserved
        Assert.assertEquals(3, results.size());

        // Check that only the last row has the aggregated values
        Row lastRow = results.get(2);
        Assert.assertEquals(600.0, (Double) lastRow.getValue("total_size_mb"), 0.001);
        Assert.assertEquals(135.0, (Double) lastRow.getValue("total_time_s"), 0.001);
    }

    @Test
    public void testAverageAggregation() throws Exception {
        String[] directives = new String[] {
                "aggregate-stats :size :time avg_size_gb avg_time_m GB m average"
        };

        List<Row> rows = Arrays.asList(
                createRow("1024MB", "30s"),
                createRow("2048MB", "90s"),
                createRow("3072MB", "120s"));

        List<Row> results = TestingRig.execute(directives, rows);

        Row lastRow = results.get(2);
        // Average: (1 + 2 + 3)GB / 3 = 2GB
        Assert.assertEquals(2.0, (Double) lastRow.getValue("avg_size_gb"), 0.001);
        // Average: (0.5 + 1.5 + 2)m / 3 = 1.333m
        Assert.assertEquals(1.333333, (Double) lastRow.getValue("avg_time_m"), 0.001);
    }

    @Test
    public void testMixedInputUnits() throws Exception {
        String[] directives = new String[] {
                "aggregate-stats :size :time total_size_gb total_time_m GB m"
        };

        List<Row> rows = Arrays.asList(
                createRow("1024MB", "60s"),
                createRow("2GB", "120s"),
                createRow("3072MB", "180s"));

        List<Row> results = TestingRig.execute(directives, rows);

        Row lastRow = results.get(2);
        // Total: 1GB + 2GB + 3GB = 6GB
        Assert.assertEquals(6.0, (Double) lastRow.getValue("total_size_gb"), 0.001);
        // Total: 1m + 2m + 3m = 6m
        Assert.assertEquals(6.0, (Double) lastRow.getValue("total_time_m"), 0.001);
    }

    @Test(expected = DirectiveExecutionException.class)
    public void testInvalidSizeUnit() throws Exception {
        String[] directives = new String[] {
                "aggregate-stats :size :time total_size total_time PB s" // Invalid PB unit
        };

        List<Row> rows = Arrays.asList(
                createRow("100MB", "30s"));

        TestingRig.execute(directives, rows);
    }

    @Test(expected = DirectiveExecutionException.class)
    public void testInvalidTimeUnit() throws Exception {
        String[] directives = new String[] {
                "aggregate-stats :size :time total_size total_time GB w" // Invalid w unit
        };

        List<Row> rows = Arrays.asList(
                createRow("100MB", "30s"));

        TestingRig.execute(directives, rows);
    }

    private Row createRow(String size, String time) {
        Row row = new Row();
        row.add("size", size);
        row.add("time", time);
        return row;
    }
}