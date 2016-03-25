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

import static com.asakusafw.dag.compiler.codegen.AsmUtil.*;

import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.asakusafw.dag.compiler.codegen.AsmUtil.FieldRef;
import com.asakusafw.dag.compiler.model.ClassData;
import com.asakusafw.dag.compiler.model.graph.VertexElement;
import com.asakusafw.dag.runtime.adapter.ExtractOperation;
import com.asakusafw.dag.utils.common.Arguments;
import com.asakusafw.dag.utils.common.Lang;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.runtime.core.Result;

/**
 * Generates an adapter between {@link ExtractOperation} and {@link Result}.
 */
public class ExtractAdapterGenerator {

    private static final ClassDescription RESULT_TYPE = Descriptions.classOf(Result.class);

    /**
     * Generates the class.
     * @param successor the successor
     * @param target the target class
     * @return the generated class
     */
    public ClassData generate(VertexElement successor, ClassDescription target) {
        Arguments.require(successor.getRuntimeType().equals(RESULT_TYPE));
        ClassWriter writer = newWriter(target, Object.class, Result.class);
        Map<VertexElement, FieldRef> deps = defineDependenciesConstructor(
                target, writer, Arrays.asList(successor), Lang.discard());
        defineResultAdd(writer, method -> {
            deps.get(successor).load(method);
            method.visitVarInsn(Opcodes.ALOAD, 1);
            method.visitTypeInsn(Opcodes.CHECKCAST, typeOf(ExtractOperation.Input.class).getInternalName());
            method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                    typeOf(ExtractOperation.Input.class).getInternalName(),
                    "getObject",
                    Type.getMethodDescriptor(typeOf(Object.class)),
                    true);
            invokeResultAdd(method);
        });
        return new ClassData(target, writer::toByteArray);
    }
}
