/**
 * Copyright 2011-2016 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.dag.api.counter.basic;

import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.asakusafw.dag.api.counter.CounterGroup;
import com.asakusafw.dag.utils.common.Arguments;

/**
 * An abstract implementation of {@link CounterGroup}.
 */
public abstract class AbstractCounterGroup implements CounterGroup {

    private final ConcurrentMap<CounterGroup.Column, AtomicLong> counters = new ConcurrentHashMap<>();

    /**
     * Registers a column and returns the new counter entity for the column.
     * @param column the target column
     * @return the created counter entity
     */
    public final AtomicLong register(CounterGroup.Column column) {
        Arguments.requireNonNull(column);
        AtomicLong counter = new AtomicLong();
        if (counters.putIfAbsent(column, counter) == null) {
            return counter;
        } else {
            throw new IllegalStateException(MessageFormat.format(
                    "column is already registered: {0}",
                    column));
        }
    }

    @Override
    public long getCount(Column column) {
        Arguments.requireNonNull(column);
        AtomicLong counter = counters.get(column);
        if (counter == null) {
            throw new NoSuchElementException(column.toString());
        }
        return counter.get();
    }

    @Override
    public String toString() {
        return counters.toString();
    }
}
