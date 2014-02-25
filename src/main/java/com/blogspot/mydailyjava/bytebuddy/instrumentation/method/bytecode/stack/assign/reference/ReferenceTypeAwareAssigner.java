package com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.assign.reference;

import com.blogspot.mydailyjava.bytebuddy.instrumentation.Instrumentation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.assign.Assigner;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.IllegalStackManipulation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.LegalTrivialStackManipulation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.type.TypeDescription;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public enum ReferenceTypeAwareAssigner implements Assigner {
    INSTANCE;

    private static class DownCastStackManipulation implements StackManipulation {

        private final String targetTypeInternalName;

        private DownCastStackManipulation(TypeDescription targetType) {
            this.targetTypeInternalName = targetType.getInternalName();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Size apply(MethodVisitor methodVisitor, Instrumentation.Context instrumentationContext) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, targetTypeInternalName);
            return StackSize.ZERO.toIncreasingSize();
        }
    }

    @Override
    public StackManipulation assign(TypeDescription sourceType, TypeDescription targetType, boolean considerRuntimeType) {
        if (sourceType.isPrimitive() || targetType.isPrimitive()) {
            if (sourceType.equals(targetType)) {
                return LegalTrivialStackManipulation.INSTANCE;
            } else {
                return IllegalStackManipulation.INSTANCE;
            }
        } else if (targetType.isAssignableFrom(sourceType)) {
            return LegalTrivialStackManipulation.INSTANCE;
        } else if (considerRuntimeType) {
            return new DownCastStackManipulation(targetType);
        } else {
            return IllegalStackManipulation.INSTANCE;
        }
    }
}