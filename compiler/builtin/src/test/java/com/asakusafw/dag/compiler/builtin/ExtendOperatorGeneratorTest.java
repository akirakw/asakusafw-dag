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
package com.asakusafw.dag.compiler.builtin;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.asakusafw.dag.compiler.codegen.OperatorNodeGenerator.NodeInfo;
import com.asakusafw.dag.runtime.testing.MockDataModel;
import com.asakusafw.dag.runtime.testing.MockValueModel;
import com.asakusafw.dag.utils.common.Lang;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.graph.CoreOperator;
import com.asakusafw.lang.compiler.model.graph.CoreOperator.CoreOperatorKind;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link ExtendOperatorGenerator}.
 */
public class ExtendOperatorGeneratorTest extends OperatorNodeGeneratorTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        CoreOperator operator = CoreOperator.builder(CoreOperatorKind.EXTEND)
            .input("in", Descriptions.typeOf(MockValueModel.class))
            .output("out", Descriptions.typeOf(MockDataModel.class))
            .build();
        NodeInfo info = generate(operator);
        MockResult<MockDataModel> results = new MockResult<>();
        loading(info, ctor -> {
            Result<Object> r = ctor.newInstance(results);
            r.add(new MockValueModel("Hello, world!"));
        });
        List<String> r = Lang.project(results.getResults(), m -> m.getValue());
        assertThat(r, contains("Hello, world!"));
    }

    /**
     * each extended properties must be set to {@code null}.
     */
    @Test
    public void extend_with_null() {
        CoreOperator operator = CoreOperator.builder(CoreOperatorKind.EXTEND)
                .input("in", Descriptions.typeOf(MockValueModel.class))
                .output("out", Descriptions.typeOf(MockDataModel.class))
                .build();
        NodeInfo info = generate(operator);
        MockResult<MockDataModel> results = new MockResult<MockDataModel>() {
            @Override
            public void add(MockDataModel result) {
                assertThat(result.getKeyOption(), is(new IntOption()));
                result.setKey(-1);
                super.add(new MockDataModel(result));
            }
        };
        loading(info, ctor -> {
            Result<Object> r = ctor.newInstance(results);
            r.add(new MockValueModel("Hello0"));
            r.add(new MockValueModel("Hello1"));
            r.add(new MockValueModel("Hello2"));
        });
        List<String> r = Lang.project(results.getResults(), m -> m.getValue());
        assertThat(r, containsInAnyOrder("Hello0", "Hello1", "Hello2"));
    }
}
