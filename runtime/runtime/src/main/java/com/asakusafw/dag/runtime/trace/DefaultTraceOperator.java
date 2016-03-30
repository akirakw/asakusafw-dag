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
package com.asakusafw.dag.runtime.trace;

/**
 * A default implementation of {@code Trace} operator.
 */
public class DefaultTraceOperator {

    private static final String PREFIX = "TRACE-"; //$NON-NLS-1$

    private static final String SEPARATOR = ": "; //$NON-NLS-1$

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Adds a data trace as logging operator.
     * @param data the target data
     * @param header the report header
     * @return the trace message
     */
    public String trace(Object data, String header) {
        buffer.setLength(0);
        return buffer.append(PREFIX).append(header).append(SEPARATOR).append(data).toString();
    }
}
