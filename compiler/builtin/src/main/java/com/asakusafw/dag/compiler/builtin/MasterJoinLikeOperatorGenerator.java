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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.asakusafw.dag.compiler.codegen.AsmUtil.FieldRef;
import com.asakusafw.dag.compiler.codegen.AsmUtil.LocalVarRef;
import com.asakusafw.dag.compiler.codegen.AsmUtil.ValueRef;
import com.asakusafw.dag.compiler.codegen.OperatorNodeGenerator;
import com.asakusafw.dag.compiler.model.ClassData;
import com.asakusafw.dag.runtime.adapter.CoGroupOperation;
import com.asakusafw.dag.runtime.adapter.DataTable;
import com.asakusafw.dag.runtime.skeleton.MergeJoinResult;
import com.asakusafw.dag.runtime.skeleton.TableJoinResult;
import com.asakusafw.dag.utils.common.Invariants;
import com.asakusafw.dag.utils.common.Lang;
import com.asakusafw.lang.compiler.analyzer.util.MasterJoinOperatorUtil;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.graph.OperatorInput;
import com.asakusafw.lang.compiler.model.graph.OperatorProperty;
import com.asakusafw.lang.compiler.model.graph.UserOperator;
import com.asakusafw.vocabulary.operator.MasterJoin;

/**
 * An abstract implementation of {@link OperatorNodeGenerator} for master join like operators.
 */
public abstract class MasterJoinLikeOperatorGenerator extends UserOperatorNodeGenerator {

    @Override
    protected final NodeInfo generate(Context context, UserOperator operator, ClassDescription target) {
        checkPorts(operator, i -> i == 2, i -> i >= 1);

        OperatorInput master = operator.getInputs().get(MasterJoin.ID_INPUT_MASTER);
        if (context.isSideData(master)) {
            return generateTableJoin(context, operator, target);
        } else {
            return generateMergeJoin(context, operator, target);
        }
    }

    private NodeInfo generateMergeJoin(Context context, UserOperator operator, ClassDescription target) {
        OperatorInput master = operator.getInputs().get(MasterJoin.ID_INPUT_MASTER);
        OperatorInput transaction = operator.getInputs().get(MasterJoin.ID_INPUT_TRANSACTION);
        ClassWriter writer = newWriter(target, MergeJoinResult.class);
        FieldRef impl = defineOperatorField(writer, operator, target);
        Consumer<MethodVisitor> initializer = defineExtraFields(writer, context, operator, target);
        Map<OperatorProperty, FieldRef> dependencies = defineConstructor(
                context, operator,
                target, writer,
                method -> {
                    method.visitVarInsn(Opcodes.ALOAD, 0);
                    getInt(method, context.getGroupIndex(master));
                    getInt(method, context.getGroupIndex(transaction));
                    method.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            typeOf(MergeJoinResult.class).getInternalName(),
                            CONSTRUCTOR_NAME,
                            Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE),
                            false);
                },
                method -> {
                    setOperatorField(method, operator, impl);
                    initializer.accept(method);
                });
        defineSelection(context, writer, operator, impl, dependencies);
        defineProcess(context, writer, operator, impl, dependencies, target);
        writer.visitEnd();
        return new OperatorNodeInfo(
                new ClassData(target, writer::toByteArray),
                Descriptions.typeOf(CoGroupOperation.Input.class),
                getDefaultDependencies(context, operator));
    }

    private NodeInfo generateTableJoin(Context context, UserOperator operator, ClassDescription target) {
        OperatorInput master = operator.getInputs().get(MasterJoin.ID_INPUT_MASTER);
        OperatorInput transaction = operator.getInputs().get(MasterJoin.ID_INPUT_TRANSACTION);
        List<OperatorProperty> injects = new ArrayList<>();
        injects.add(master);
        injects.addAll(operator.getOutputs());
        injects.addAll(operator.getArguments());

        ClassWriter writer = newWriter(target, TableJoinResult.class);
        FieldRef impl = defineOperatorField(writer, operator, target);
        Consumer<MethodVisitor> initializer = defineExtraFields(writer, context, operator, target);
        Map<OperatorProperty, FieldRef> dependencies = defineConstructor(
                context, injects,
                target, writer,
                method -> {
                    method.visitVarInsn(Opcodes.ALOAD, 0);
                    method.visitVarInsn(Opcodes.ALOAD, 1); // the first argument is data table
                    method.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            typeOf(TableJoinResult.class).getInternalName(),
                            CONSTRUCTOR_NAME,
                            Type.getMethodDescriptor(Type.VOID_TYPE, typeOf(DataTable.class)),
                            false);
                },
                method -> {
                    setOperatorField(method, operator, impl);
                    initializer.accept(method);
                });
        defineBuildKey(context, writer, transaction.getDataType(), transaction.getGroup());
        defineSelection(context, writer, operator, impl, dependencies);
        defineProcess(context, writer, operator, impl, dependencies, target);
        writer.visitEnd();
        return new OperatorNodeInfo(
                new ClassData(target, writer::toByteArray),
                transaction.getDataType(),
                context.getDependencies(injects));
    }

    /**
     * Defines extra fields.
     * @param writer the writer
     * @param context the context
     * @param operator the target operator
     * @param target the current class
     * @return the field initializer
     */
    protected Consumer<MethodVisitor> defineExtraFields(
            ClassVisitor writer, Context context,
            UserOperator operator, ClassDescription target) {
        return v -> Lang.discard();
    }

    private void defineSelection(
            Context context,
            ClassWriter writer,
            UserOperator operator,
            FieldRef impl,
            Map<OperatorProperty, FieldRef> dependencies) {
        Method selector = Invariants.safe(() -> {
            return MasterJoinOperatorUtil.getSelection(context.getClassLoader(), operator);
        });
        if (selector == null) {
            return;
        }
        MethodVisitor method = writer.visitMethod(
                Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL,
                "selectMaster",
                Type.getMethodDescriptor(typeOf(Object.class), typeOf(List.class), typeOf(Object.class)),
                null,
                null);
        cast(method, 2, operator.getInputs().get(MasterJoin.ID_INPUT_TRANSACTION).getDataType());

        List<ValueRef> arguments = new ArrayList<>();
        impl.load(method);
        arguments.add(new LocalVarRef(Opcodes.ALOAD, 1));
        arguments.add(new LocalVarRef(Opcodes.ALOAD, 2));
        arguments.addAll(Lang.project(operator.getArguments(), v -> Invariants.requireNonNull(dependencies.get(v))));
        for (int i = 0, n = selector.getParameterCount(); i < n; i++) {
            arguments.get(i).load(method);
        }
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                typeOf(selector.getDeclaringClass()).getInternalName(),
                selector.getName(),
                Type.getMethodDescriptor(selector),
                false);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private void defineProcess(
            Context context,
            ClassWriter writer,
            UserOperator operator,
            FieldRef impl,
            Map<OperatorProperty, FieldRef> dependencies,
            ClassDescription target) {
        MethodVisitor method = writer.visitMethod(
                Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL,
                "process",
                Type.getMethodDescriptor(Type.VOID_TYPE, typeOf(Object.class), typeOf(Object.class)),
                null,
                null);
        cast(method, 1, operator.getInputs().get(MasterJoin.ID_INPUT_MASTER).getDataType());
        cast(method, 2, operator.getInputs().get(MasterJoin.ID_INPUT_TRANSACTION).getDataType());
        defineProcess(method, context, operator,
                new LocalVarRef(Opcodes.ALOAD, 1), new LocalVarRef(Opcodes.ALOAD, 2),
                impl, dependencies, target);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }


    /**
     * Defines the body of {@code process} method.
     * @param method the method writer
     * @param context the current context
     * @param operator the target operator
     * @param master the master input
     * @param transaction the transaction input
     * @param impl the operator implementation
     * @param dependencies the dependency map
     * @param target the target class
     */
    protected abstract void defineProcess(
            MethodVisitor method,
            Context context,
            UserOperator operator,
            LocalVarRef master, LocalVarRef transaction,
            FieldRef impl,
            Map<OperatorProperty, FieldRef> dependencies,
            ClassDescription target);
}
