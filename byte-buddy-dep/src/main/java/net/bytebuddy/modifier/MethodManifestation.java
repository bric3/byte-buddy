package net.bytebuddy.modifier;

import net.bytebuddy.instrumentation.ModifierContributor;
import org.objectweb.asm.Opcodes;

/**
 * Describes the manifestation of a method, i.e. if a method is final, abstract or native.
 * Note that an {@code abstract} method must never be static and can only be declared for an
 * {@code abstract} type.
 */
public enum MethodManifestation implements ModifierContributor.ForMethod {

    PLAIN(EMPTY_MASK),
    NATIVE(Opcodes.ACC_NATIVE),
    ABSTRACT(Opcodes.ACC_ABSTRACT),
    FINAL(Opcodes.ACC_FINAL),
    FINAL_NATIVE(Opcodes.ACC_FINAL | Opcodes.ACC_NATIVE);

    /**
     * A mask for checking if a method implementation is not implemented in byte code.
     */
    public static final int ABSTRACTION_MASK = Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE;

    private final int mask;

    private MethodManifestation(int mask) {
        this.mask = mask;
    }

    @Override
    public int getMask() {
        return mask;
    }
}
