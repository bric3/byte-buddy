package net.bytebuddy.instrumentation.method.bytecode.stack.member;

import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.utility.MockitoRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class MethodArgumentTest {

    private final TypeDescription typeDescription;
    private final int opcode;
    private final int size;
    @Rule
    public TestRule mockitoRule = new MockitoRule(this);
    @Mock
    private MethodVisitor methodVisitor;
    @Mock
    private Instrumentation.Context instrumentationContext;

    public MethodArgumentTest(Class<?> type, int opcode, int size) {
        this.typeDescription = mock(TypeDescription.class);
        when(typeDescription.isPrimitive()).thenReturn(type.isPrimitive());
        when(typeDescription.represents(type)).thenReturn(true);
        this.opcode = opcode;
        this.size = size;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Object.class, Opcodes.ALOAD, 1},
                {boolean.class, Opcodes.ILOAD, 1},
                {byte.class, Opcodes.ILOAD, 1},
                {short.class, Opcodes.ILOAD, 1},
                {char.class, Opcodes.ILOAD, 1},
                {int.class, Opcodes.ILOAD, 1},
                {long.class, Opcodes.LLOAD, 2},
                {float.class, Opcodes.FLOAD, 1},
                {double.class, Opcodes.DLOAD, 2},
        });
    }

    @After
    public void setUp() throws Exception {
        verifyZeroInteractions(instrumentationContext);
    }

    @Test
    public void testLoading() throws Exception {
        StackManipulation stackManipulation = MethodVariableAccess.forType(typeDescription).loadFromIndex(4);
        assertThat(stackManipulation.isValid(), is(true));
        StackManipulation.Size size = stackManipulation.apply(methodVisitor, instrumentationContext);
        assertThat(size.getSizeImpact(), is(this.size));
        assertThat(size.getMaximalSize(), is(this.size));
        verify(methodVisitor).visitVarInsn(opcode, 4);
        verifyNoMoreInteractions(methodVisitor);
    }
}
