package net.bytebuddy.instrumentation.type.auxiliary;

import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.ClassLoadingStrategy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.utility.MockitoRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.objectweb.asm.Opcodes;

import java.util.concurrent.Callable;

import static net.bytebuddy.instrumentation.method.matcher.MethodMatchers.isConstructor;
import static net.bytebuddy.instrumentation.method.matcher.MethodMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AbstractMethodCallProxyTest {

    protected static final String FOO = "foo";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private MethodDescription targetMethod;
    @Mock
    private AuxiliaryType.MethodAccessorFactory methodAccessorFactory;

    protected Class<?> proxyOnlyDeclaredMethodOf(Class<?> proxyTarget) throws Exception {
        MethodDescription proxyMethod = new TypeDescription.ForLoadedType(proxyTarget)
                .getDeclaredMethods().filter(not(isConstructor())).getOnly();
        when(methodAccessorFactory.requireAccessorMethodFor(targetMethod)).thenReturn(proxyMethod);
        String auxiliaryTypeName = getClass().getName() + "$" + proxyTarget.getSimpleName() + "$Proxy";
        DynamicType dynamicType = new MethodCallProxy(targetMethod).make(auxiliaryTypeName,
                ClassFileVersion.forCurrentJavaVersion(),
                methodAccessorFactory);
        DynamicType.Unloaded<?> unloaded = (DynamicType.Unloaded<?>) dynamicType;
        Class<?> auxiliaryType = unloaded.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
        assertThat(auxiliaryType.getName(), is(auxiliaryTypeName));
        verify(methodAccessorFactory).requireAccessorMethodFor(targetMethod);
        verifyNoMoreInteractions(methodAccessorFactory);
        verifyZeroInteractions(targetMethod);
        assertThat(auxiliaryType.getModifiers(), is(Opcodes.ACC_SYNTHETIC));
        assertThat(Callable.class.isAssignableFrom(auxiliaryType), is(true));
        assertThat(Runnable.class.isAssignableFrom(auxiliaryType), is(true));
        assertThat(auxiliaryType.getDeclaredConstructors().length, is(1));
        assertThat(auxiliaryType.getDeclaredMethods().length, is(2));
        assertThat(auxiliaryType.getDeclaredFields().length,
                is(proxyMethod.getParameterTypes().size() + (proxyMethod.isStatic() ? 0 : 1)));
        int fieldIndex = 0;
        if (!proxyMethod.isStatic()) {
            assertEquals(proxyTarget, auxiliaryType.getDeclaredFields()[fieldIndex++].getType());
        }
        for (Class<?> parameterType : proxyTarget.getDeclaredMethods()[0].getParameterTypes()) {
            assertEquals(parameterType, auxiliaryType.getDeclaredFields()[fieldIndex++].getType());
        }
        return auxiliaryType;
    }
}
