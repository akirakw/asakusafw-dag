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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

import com.asakusafw.dag.api.processor.testing.CollectionObjectReader;
import com.asakusafw.dag.api.processor.testing.MockVertexProcessorContext;
import com.asakusafw.dag.runtime.adapter.DataTable;
import com.asakusafw.dag.runtime.adapter.DataTableAdapter;
import com.asakusafw.dag.runtime.adapter.KeyBuffer;
import com.asakusafw.dag.runtime.testing.MockDataModel;
import com.asakusafw.dag.utils.common.Action;
import com.asakusafw.dag.utils.common.Lang;
import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link EdgeDataTableAdapter}.
 */
public class EdgeDataTableAdapterTest {

    private final List<Consumer<EdgeDataTableAdapter>> specs = new ArrayList<>();

    private final Map<String, List<Object>> inputs = new LinkedHashMap<>();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        define("t", "i", MockDataModel.class, "key");
        data("i", new Object[] {
                new MockDataModel(0, "Hello0"),
                new MockDataModel(1, "Hello1a"),
                new MockDataModel(1, "Hello1b"),
                new MockDataModel(2, "Hello2"),
        });
        check(a -> {
            DataTable<MockDataModel> t = a.getDataTable(MockDataModel.class, "t");
            assertThat(get(t, MockDataModel::getValue, 0), containsInAnyOrder("Hello0"));
            assertThat(get(t, MockDataModel::getValue, 1), containsInAnyOrder("Hello1a", "Hello1b"));
            assertThat(get(t, MockDataModel::getValue, 2), containsInAnyOrder("Hello2"));
            assertThat(get(t, MockDataModel::getValue, 3), hasSize(0));
        });
    }

    /**
     * multiple tables.
     */
    @Test
    public void multiple() {
        define("t0", "i0", MockDataModel.class, "key");
        define("t1", "i1", MockDataModel.class, "key");
        data("i0", new Object[] {
                new MockDataModel(0, "Hello0"),
        });
        data("i1", new Object[] {
                new MockDataModel(1, "Hello1"),
        });
        check(a -> {
            DataTable<MockDataModel> t0 = a.getDataTable(MockDataModel.class, "t0");
            assertThat(get(t0, MockDataModel::getValue, 0), containsInAnyOrder("Hello0"));
            assertThat(get(t0, MockDataModel::getValue, 1), hasSize(0));

            DataTable<MockDataModel> t1 = a.getDataTable(MockDataModel.class, "t1");
            assertThat(get(t1, MockDataModel::getValue, 0), hasSize(0));
            assertThat(get(t1, MockDataModel::getValue, 1), containsInAnyOrder("Hello1"));
        });
    }

    private void define(String tId, String iId, Class<?> type, String... group) {
        specs.add(a -> a.bind(tId, iId, MockDataModel.KeyBuilder.class, MockDataModel.Copier.class));
    }

    private void data(String iId, Object... values) {
        inputs.put(iId, Arrays.asList(values));
    }

    private void check(Action<DataTableAdapter, Exception> callback) {
        MockVertexProcessorContext context = new MockVertexProcessorContext();
        inputs.forEach((k, v) -> {
            context.withInput(k, () -> new CollectionObjectReader(v));
        });
        try (EdgeDataTableAdapter adapter = new EdgeDataTableAdapter(context)) {
            specs.forEach(s -> s.accept(adapter));
            adapter.initialize();
            callback.perform(adapter);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private <T, U> List<U> get(DataTable<T> table, Function<T, U> mapper, int... key) {
        List<T> list = table.getList(key(table, key));
        return Lang.project(list, mapper);
    }

    private KeyBuffer key(DataTable<?> table, int... values) {
        KeyBuffer key = table.newKeyBuffer();
        for (int value : values) {
            key.append(new IntOption(value));
        }
        return key;
    }
}
