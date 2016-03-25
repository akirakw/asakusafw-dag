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

import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.asakusafw.dag.compiler.codegen.AsmUtil.FieldRef;
import com.asakusafw.dag.compiler.codegen.AsmUtil.LocalVarRef;
import com.asakusafw.dag.compiler.model.ClassData;
import com.asakusafw.dag.compiler.model.graph.DataNode;
import com.asakusafw.dag.compiler.model.graph.VertexElement;
import com.asakusafw.dag.utils.common.Arguments;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.description.ReifiableTypeDescription;
import com.asakusafw.lang.compiler.model.description.TypeDescription;
import com.asakusafw.runtime.core.Result;

/**
 * Generates buffer operator classes.
 */
public class BufferOperatorGenerator {

    private static final ClassDescription RESULT_TYPE = Descriptions.classOf(Result.class);

    /**
     * Generates buffer operator class.
     * @param successors the successors
     * @param target the target class
     * @return the generated class
     */
    public ClassData generate(List<VertexElement> successors, ClassDescription target) {
        Arguments.require(successors.size() >= 2);
        TypeDescription dataType = getDataType(successors);
        ClassWriter writer = newWriter(target, Object.class, Result.class);
        FieldRef buffer = defineField(writer, target, "buffer", typeOf(dataType));
        Map<VertexElement, FieldRef> deps = defineDependenciesConstructor(target, writer, successors, v -> {
            v.visitVarInsn(Opcodes.ALOAD, 0);
            getNew(v, dataType);
            putField(v, buffer);
        });
        defineResultAdd(writer, method -> {
            LocalVarRef self = new LocalVarRef(Opcodes.ALOAD, 0);
            LocalVarRef input = cast(method, 1, dataType);
            self.load(method);
            getField(method, buffer);
            LocalVarRef buf = putLocalVar(method, Type.OBJECT, 2);
            for (int i = 0, n = successors.size(); i < n; i++) {
                self.load(method);
                getField(method, deps.get(successors.get(i)));
                if (i < n - 1) {
                    buf.load(method);
                    input.load(method);
                    copyDataModel(method, dataType);
                    buf.load(method);
                } else {
                    input.load(method);
                }
                invokeResultAdd(method);
            }
        });
        writer.visitEnd();
        return new ClassData(target, writer::toByteArray);
    }

    private TypeDescription getDataType(List<VertexElement> successors) {
        TypeDescription dataType = null;
        for (VertexElement element : successors) {
            Arguments.require(element instanceof DataNode);
            DataNode sink = (DataNode) element;
            if (dataType == null) {
                dataType = sink.getDataType().getErasure();
            } else {
                Arguments.require(dataType.equals(sink.getDataType()));
            }
            ReifiableTypeDescription erasure = element.getRuntimeType().getErasure();
            Arguments.require(erasure.equals(RESULT_TYPE));
        }
        return dataType;
    }
}
