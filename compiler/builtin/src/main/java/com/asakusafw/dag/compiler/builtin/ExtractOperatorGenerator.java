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

import static com.asakusafw.dag.compiler.builtin.Util.*;
import static com.asakusafw.dag.compiler.codegen.AsmUtil.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import com.asakusafw.dag.compiler.codegen.AsmUtil.FieldRef;
import com.asakusafw.dag.compiler.codegen.AsmUtil.LocalVarRef;
import com.asakusafw.dag.compiler.codegen.AsmUtil.ValueRef;
import com.asakusafw.dag.compiler.model.ClassData;
import com.asakusafw.dag.utils.common.Lang;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.graph.OperatorInput;
import com.asakusafw.lang.compiler.model.graph.OperatorProperty;
import com.asakusafw.lang.compiler.model.graph.UserOperator;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.operator.Extract;

/**
 * Generates {@link Extract} operator.
 */
public class ExtractOperatorGenerator extends UserOperatorNodeGenerator {

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return Extract.class;
    }

    @Override
    protected NodeInfo generate(Context context, UserOperator operator, ClassDescription target) {
        checkPorts(operator, i -> i == 1, i -> i >= 1);

        OperatorInput input = operator.getInputs().get(0);
        ClassWriter writer = newWriter(target, Object.class, Result.class);
        FieldRef impl = defineOperatorField(writer, operator, target);
        Map<OperatorProperty, FieldRef> map = defineConstructor(context, operator, target, writer, method -> {
            setOperatorField(method, operator, impl);
        });
        defineResultAdd(writer, method -> {
            cast(method, 1, input.getDataType());
            List<ValueRef> arguments = new ArrayList<>();
            arguments.add(impl);
            arguments.add(new LocalVarRef(Opcodes.ALOAD, 1));
            arguments.addAll(Lang.project(operator.getOutputs(), e -> map.get(e)));
            arguments.addAll(Lang.project(operator.getArguments(), e -> map.get(e)));
            invoke(method, context, operator, arguments);
        });
        return new OperatorNodeInfo(
                new ClassData(target, writer::toByteArray),
                input.getDataType(),
                getDefaultDependencies(context, operator));
    }
}
