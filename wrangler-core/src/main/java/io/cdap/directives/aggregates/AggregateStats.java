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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive for aggregating byte sizes and time durations.
 */
@Plugin(type = Directive.TYPE)
@Name(AggregateStats.NAME)
@Description("Aggregates byte sizes and time durations into totals or averages with unit conversion")
public class AggregateStats implements Directive {
    public static final String NAME = "aggregate-stats";

    private static final String TOTAL_BYTES_KEY = "total_bytes";
    private static final String TOTAL_NANOS_KEY = "total_nanos";
    private static final String ROW_COUNT_KEY = "row_count";

    private String sourceSizeColumn;
    private String sourceTimeColumn;
    private String targetSizeColumn;
    private String targetTimeColumn;
    private String sizeUnit;
    private String timeUnit;
    private boolean isAverage;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
        builder.define("sourceSizeColumn", TokenType.COLUMN_NAME);
        builder.define("sourceTimeColumn", TokenType.COLUMN_NAME);
        builder.define("targetSizeColumn", TokenType.COLUMN_NAME);
        builder.define("targetTimeColumn", TokenType.COLUMN_NAME);
        builder.define("sizeUnit", TokenType.TEXT, "Output unit for size (KB, MB, GB, TB)");
        builder.define("timeUnit", TokenType.TEXT, "Output unit for time (ms, s, m, h, d)");
        builder.define("aggregationType", TokenType.TEXT, "Optional: Type of aggregation (total or average)", true);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sourceSizeColumn = ((ColumnName) args.value("sourceSizeColumn")).value();
        this.sourceTimeColumn = ((ColumnName) args.value("sourceTimeColumn")).value();
        this.targetSizeColumn = ((ColumnName) args.value("targetSizeColumn")).value();
        this.targetTimeColumn = ((ColumnName) args.value("targetTimeColumn")).value();
        this.sizeUnit = ((Text) args.value("sizeUnit")).value();
        this.timeUnit = ((Text) args.value("timeUnit")).value();

        String aggType = args.value("aggregationType") != null
                ? ((Text) args.value("aggregationType")).value().toLowerCase()
                : "total";
        this.isAverage = "average".equals(aggType);
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        TransientStore store = context.getTransientStore();

        // Process each row and accumulate values
        for (Row row : rows) {
            // Get current values from store or initialize to 0
            long currentBytes = store.get(TOTAL_BYTES_KEY) != null ? (Long) store.get(TOTAL_BYTES_KEY) : 0L;
            long currentNanos = store.get(TOTAL_NANOS_KEY) != null ? (Long) store.get(TOTAL_NANOS_KEY) : 0L;
            long rowCount = store.get(ROW_COUNT_KEY) != null ? (Long) store.get(ROW_COUNT_KEY) : 0L;

            // Parse and add size value
            String sizeStr = (String) row.getValue(sourceSizeColumn);
            if (sizeStr != null) {
                ByteSize size = ByteSize.parse(sizeStr);
                currentBytes += size.toBytes();
            }

            // Parse and add time value
            String timeStr = (String) row.getValue(sourceTimeColumn);
            if (timeStr != null) {
                TimeDuration duration = TimeDuration.parse(timeStr);
                currentNanos += duration.toNanos();
            }

            rowCount++;

            // Store updated values
            store.set(TransientVariableScope.GLOBAL, TOTAL_BYTES_KEY, currentBytes);
            store.set(TransientVariableScope.GLOBAL, TOTAL_NANOS_KEY, currentNanos);
            store.set(TransientVariableScope.GLOBAL, ROW_COUNT_KEY, rowCount);
        }

        // On the last row, calculate final values and add to row
        Row lastRow = rows.get(rows.size() - 1);

        // Get final totals
        long totalBytes = (Long) store.get(TOTAL_BYTES_KEY);
        long totalNanos = (Long) store.get(TOTAL_NANOS_KEY);
        long rowCount = (Long) store.get(ROW_COUNT_KEY);

        // Convert to target units
        double finalSizeValue;
        double finalTimeValue;

        if (isAverage && rowCount > 0) {
            totalBytes /= rowCount;
            totalNanos /= rowCount;
        }

        // Convert bytes to target unit
        ByteSize totalSize = ByteSize.fromBytes(totalBytes);
        switch (sizeUnit.toUpperCase()) {
            case "KB":
                finalSizeValue = totalSize.toKilobytes();
                break;
            case "MB":
                finalSizeValue = totalSize.toMegabytes();
                break;
            case "GB":
                finalSizeValue = totalSize.toGigabytes();
                break;
            case "TB":
                finalSizeValue = totalSize.toTerabytes();
                break;
            default:
                throw new DirectiveExecutionException(NAME, "Invalid size unit: " + sizeUnit);
        }

        // Convert nanos to target unit
        TimeDuration totalTime = TimeDuration.fromNanos(totalNanos);
        switch (timeUnit.toLowerCase()) {
            case "ms":
                finalTimeValue = totalTime.toMillis();
                break;
            case "s":
                finalTimeValue = totalTime.toSeconds();
                break;
            case "m":
                finalTimeValue = totalTime.toMinutes();
                break;
            case "h":
                finalTimeValue = totalTime.toHours();
                break;
            case "d":
                finalTimeValue = totalTime.toDays();
                break;
            default:
                throw new DirectiveExecutionException(NAME, "Invalid time unit: " + timeUnit);
        }

        // Add results to the last row
        int sizeColIdx = lastRow.find(targetSizeColumn);
        if (sizeColIdx == -1) {
            lastRow.add(targetSizeColumn, finalSizeValue);
        } else {
            lastRow.setValue(sizeColIdx, finalSizeValue);
        }

        int timeColIdx = lastRow.find(targetTimeColumn);
        if (timeColIdx == -1) {
            lastRow.add(targetTimeColumn, finalTimeValue);
        } else {
            lastRow.setValue(timeColIdx, finalTimeValue);
        }

        return rows;
    }
}