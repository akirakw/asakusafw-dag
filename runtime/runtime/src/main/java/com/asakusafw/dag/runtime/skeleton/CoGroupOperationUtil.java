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
package com.asakusafw.dag.runtime.skeleton;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dag.api.common.ObjectCursor;
import com.asakusafw.dag.runtime.adapter.CoGroupOperation;
import com.asakusafw.dag.runtime.adapter.CoGroupOperation.Cursor;
import com.asakusafw.dag.runtime.adapter.CoGroupOperation.Input;
import com.asakusafw.dag.utils.common.Arguments;

/**
 * Utilities for {@link CoGroupOperation}.
 */
public final class CoGroupOperationUtil {

    private static final Cursor<Object> EMPTY_CURSOR = new Cursor<Object>() {
        @Override
        public boolean nextObject() throws IOException, InterruptedException {
            return false;
        }
        @Override
        public Object getObject() throws IOException, InterruptedException {
            throw new IllegalStateException();
        }
    };

    private CoGroupOperationUtil() {
        return;
    }

    /**
     * Returns an empty cursor.
     * @param <T> the element type
     * @return an empty cursor
     */
    @SuppressWarnings("unchecked")
    public static <T> Cursor<T> emptyCursor() {
        return (Cursor<T>) EMPTY_CURSOR;
    }

    /**
     * Returns a wrapped unsafe cursor.
     * @param <T> the element type
     * @param cursor the internal cursor
     * @return the wrapped cursor
     */
    public static <T> Cursor<T> wrap(ObjectCursor cursor) {
        Arguments.requireNonNull(cursor);
        return new Cursor<T>() {
            @Override
            public boolean nextObject() throws IOException, InterruptedException {
                return cursor.nextObject();
            }
            @SuppressWarnings("unchecked")
            @Override
            public T getObject() throws IOException, InterruptedException {
                return (T) cursor.getObject();
            }
        };
    }

    /**
     * Returns a co-group element.
     * @param <T> the element type
     * @param input the input
     * @param index the group index (0-origin)
     * @return the group elements, or an empty cursor if {@code index} is {@code -1}
     * @throws IOException if I/O error was occurred while reading the input
     * @throws InterruptedException if interrupted while reading the input
     */
    public static <T> Cursor<T> getCursor(Input input, int index) throws IOException, InterruptedException {
        if (index < 0) {
            return emptyCursor();
        }
        return input.getCursor(index);
    }

    /**
     * Returns a co-group element.
     * @param <T> the element type
     * @param input the input
     * @param index the group index (0-origin)
     * @return the group elements, or an empty list if {@code index} is {@code -1}
     * @throws IOException if I/O error was occurred while reading the input
     * @throws InterruptedException if interrupted while reading the input
     */
    public static <T> List<T> getList(Input input, int index) throws IOException, InterruptedException {
        if (index < 0) {
            return Collections.emptyList();
        }
        return input.getList(index);
    }
}
