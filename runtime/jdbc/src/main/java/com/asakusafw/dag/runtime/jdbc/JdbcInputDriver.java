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
package com.asakusafw.dag.runtime.jdbc;

import java.io.IOException;
import java.util.List;

import com.asakusafw.dag.api.processor.ObjectReader;

/**
 * Processes input from JDBC.
 * @since 0.2.0
 */
@FunctionalInterface
public interface JdbcInputDriver {

    /**
     * Returns input partitions.
     * @return the partitions to be read
     * @throws IOException if I/O error was occurred while computing input partitions
     * @throws InterruptedException if interrupted while computing input partitions
     */
    List<? extends Partition> getPartitions() throws IOException, InterruptedException;

    /**
     * Represents a partition of JDBC input.
     * @since 0.2.0
     */
    @FunctionalInterface
    interface Partition {

        /**
         * Creates a new reader which returns each object in the target partition.
         * @return the created reader
         * @throws IOException if I/O error was occurred while computing input partitions
         * @throws InterruptedException if interrupted while computing input partitions
         */
        ObjectReader open() throws IOException, InterruptedException;
    }
}
