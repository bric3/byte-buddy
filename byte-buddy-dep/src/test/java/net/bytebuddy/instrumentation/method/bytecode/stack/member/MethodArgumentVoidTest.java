package net.bytebuddy.instrumentation.method.bytecode.stack.member;

import net.bytebuddy.instrumentation.type.TypeDescription;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodArgumentVoidTest {

    @Test(expected = IllegalArgumentException.class)
    public void testVoidArgument() throws Exception {
        TypeDescription voidTypeDescription = mock(TypeDescription.class);
        when(voidTypeDescription.isPrimitive()).thenReturn(true);
        when(voidTypeDescription.represents(void.class)).thenReturn(true);
        MethodVariableAccess.forType(voidTypeDescription);
    }
}
