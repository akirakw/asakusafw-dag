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
package com.asakusafw.dag.compiler.directio;

import static com.asakusafw.dag.compiler.codegen.AsmUtil.*;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.asakusafw.dag.compiler.codegen.AsmUtil.LocalVarRef;
import com.asakusafw.dag.compiler.codegen.ClassGeneratorContext;
import com.asakusafw.dag.compiler.model.ClassData;
import com.asakusafw.dag.runtime.directio.DirectFileOutputSetup;
import com.asakusafw.dag.utils.common.Arguments;
import com.asakusafw.lang.compiler.model.description.ClassDescription;

/**
 * Generates {@link DirectFileOutputSetup}.
 */
public class DirectFileOutputSetupGenerator {

    /**
     * Generates {@link DirectFileOutputSetup} class.
     * @param context the current context
     * @param specs the binding specs
     * @param target the target class
     * @return the generated class data
     */
    public ClassData generate(ClassGeneratorContext context, List<Spec> specs, ClassDescription target) {
        ClassWriter writer = newWriter(target, DirectFileOutputSetup.class);
        defineEmptyConstructor(writer, DirectFileOutputSetup.class, v -> {
            LocalVarRef self = new LocalVarRef(Opcodes.ALOAD, 0);
            for (Spec spec : specs) {
                self.load(v);
                getConst(v, spec.id);
                getConst(v, spec.basePath);
                getArray(v, spec.deletePatterns.stream().toArray(String[]::new));
                v.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        target.getInternalName(), "bind", //$NON-NLS-1$
                        Type.getMethodDescriptor(typeOf(DirectFileOutputSetup.class),
                                typeOf(String.class), typeOf(String.class), typeOf(String[].class)),
                        false);
                v.visitInsn(Opcodes.POP);
            }
        });
        return new ClassData(target, writer::toByteArray);
    }

    /**
     * Represents an operation spec for {@link DirectFileOutputSetup}.
     */
    public static class Spec {

        final String id;

        final String basePath;

        final List<String> deletePatterns;

        /**
         * Creates a new instance.
         * @param id the output ID
         * @param basePath the base path
         * @param deletePatterns the delete patterns
         */
        public Spec(String id, String basePath, List<String> deletePatterns) {
            Arguments.requireNonNull(id);
            Arguments.requireNonNull(basePath);
            Arguments.requireNonNull(deletePatterns);
            this.id = id;
            this.basePath = basePath;
            this.deletePatterns = Arguments.freeze(deletePatterns);
        }
    }
}
