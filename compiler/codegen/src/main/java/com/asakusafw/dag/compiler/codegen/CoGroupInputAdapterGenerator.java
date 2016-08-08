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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.asakusafw.dag.compiler.model.ClassData;
import com.asakusafw.dag.runtime.skeleton.CoGroupInputAdapter;
import com.asakusafw.dag.runtime.skeleton.ExtractInputAdapter;
import com.asakusafw.dag.utils.common.Arguments;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.TypeDescription;

/**
 * Generates {@link CoGroupInputAdapter}.
 */
public class CoGroupInputAdapterGenerator {

    /**
     * Generates {@link ExtractInputAdapter} class.
     * @param context the current context
     * @param inputs the target output ports
     * @param target the target class
     * @return the generated class data
     */
    public ClassData generate(ClassGeneratorContext context, List<Spec> inputs, ClassDescription target) {
        ClassWriter writer = AsmUtil.newWriter(target, CoGroupInputAdapter.class);
        defineAdapterConstructor(writer, CoGroupInputAdapter.class, v -> {
            for (Spec spec : inputs) {
                ClassDescription supplier = SupplierGenerator.get(context, spec.dataType);
                v.visitVarInsn(Opcodes.ALOAD, 0);
                getConst(v, spec.id);
                getConst(v, typeOf(supplier));
                getEnumConstant(v, getBufferType(spec));
                v.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        target.getInternalName(), "bind", //$NON-NLS-1$
                        Type.getMethodDescriptor(
                                typeOf(CoGroupInputAdapter.class),
                                typeOf(String.class),
                                typeOf(Class.class),
                                typeOf(CoGroupInputAdapter.BufferType.class)),
                        false);
                v.visitInsn(Opcodes.POP);
            }
        });
        return new ClassData(target, writer::toByteArray);
    }

    private CoGroupInputAdapter.BufferType getBufferType(Spec spec) {
        return spec.fileListBuffer ? CoGroupInputAdapter.BufferType.FILE : CoGroupInputAdapter.BufferType.HEAP;
    }

    /**
     * Represents an input spec for co-group-kind vertices.
     */
    public static class Spec {

        final String id;

        final TypeDescription dataType;

        final boolean fileListBuffer;

        /**
         * Creates a new instance.
         * @param id the input ID
         * @param dataType the input data type
         * @param fileListBuffer {@code true} to use file list buffer
         */
        public Spec(String id, TypeDescription dataType, boolean fileListBuffer) {
            Arguments.requireNonNull(id);
            Arguments.requireNonNull(dataType);
            this.id = id;
            this.dataType = dataType;
            this.fileListBuffer = fileListBuffer;
        }
    }
}
