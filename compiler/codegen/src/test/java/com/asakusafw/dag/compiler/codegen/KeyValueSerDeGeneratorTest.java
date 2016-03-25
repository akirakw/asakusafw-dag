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
package com.asakusafw.dag.compiler.codegen;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.dag.api.common.KeyValueSerDe;
import com.asakusafw.dag.runtime.testing.MockDataModel;
import com.asakusafw.lang.compiler.api.reference.DataModelReference;
import com.asakusafw.lang.compiler.api.testing.MockDataModelLoader;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.graph.Group;
import com.asakusafw.lang.compiler.model.graph.Groups;
import com.asakusafw.runtime.io.util.DataBuffer;

/**
 * Test for {@link KeyValueSerDeGenerator}.
 */
@SuppressWarnings("deprecation")
public class KeyValueSerDeGeneratorTest extends ClassGeneratorTestRoot {

    private final MockDataModelLoader loader = new MockDataModelLoader(getClass().getClassLoader());

    /**
     * simple case.
     */
    @Test
    public void simple() {
        KeyValueSerDeGenerator generator = new KeyValueSerDeGenerator();
        Group group = Groups.parse(Arrays.asList("key"), Arrays.asList("+sort"));
        DataModelReference ref = loader.load(Descriptions.classOf(MockDataModel.class));
        ClassDescription gen = add(c -> generator.generate(ref, group, c));
        loading(cl -> {
            KeyValueSerDe object = (KeyValueSerDe) gen.resolve(cl).newInstance();

            MockDataModel model = new MockDataModel();
            model.getKeyOption().modify(100);
            model.getSortOption().modify(new BigDecimal("3.14"));
            model.getValueOption().modify("Hello, world!");

            DataBuffer kBuffer = new DataBuffer();
            DataBuffer vBuffer = new DataBuffer();
            object.serializeKey(model, kBuffer);
            object.serializeValue(model, vBuffer);

            MockDataModel copy = (MockDataModel) object.deserializePair(kBuffer, vBuffer);
            assertThat(kBuffer.getReadRemaining(), is(0));
            assertThat(vBuffer.getReadRemaining(), is(0));
            assertThat(copy, is(not(sameInstance(model))));
            assertThat(copy.getKeyOption(), is(model.getKeyOption()));
            assertThat(copy.getSortOption(), is(model.getSortOption()));
            assertThat(copy.getValueOption(), is(model.getValueOption()));
        });
    }

    /**
     * simple case.
     */
    @Test
    public void full() {
        KeyValueSerDeGenerator generator = new KeyValueSerDeGenerator();
        Group group = Groups.parse(Arrays.asList("key"), Arrays.asList("+sort", "-value"));
        DataModelReference ref = loader.load(Descriptions.classOf(MockDataModel.class));
        ClassDescription gen = add(c -> generator.generate(ref, group, c));
        loading(cl -> {
            KeyValueSerDe object = (KeyValueSerDe) gen.resolve(cl).newInstance();

            MockDataModel model = new MockDataModel();
            model.getKeyOption().modify(100);
            model.getSortOption().modify(new BigDecimal("3.14"));
            model.getValueOption().modify("Hello, world!");

            DataBuffer kBuffer = new DataBuffer();
            DataBuffer vBuffer = new DataBuffer();
            object.serializeKey(model, kBuffer);
            object.serializeValue(model, vBuffer);

            MockDataModel copy = (MockDataModel) object.deserializePair(kBuffer, vBuffer);
            assertThat(kBuffer.getReadRemaining(), is(0));
            assertThat(vBuffer.getReadRemaining(), is(0));
            assertThat(copy, is(not(sameInstance(model))));
            assertThat(copy.getKeyOption(), is(model.getKeyOption()));
            assertThat(copy.getSortOption(), is(model.getSortOption()));
            assertThat(copy.getValueOption(), is(model.getValueOption()));
        });
    }
}
