package net.bytebuddy.instrumentation;

import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.instrumentation.type.auxiliary.AuxiliaryType;
import net.bytebuddy.utility.MockitoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class InstrumentationContextDefaultTest {

    private static final String FOO = "qux", BAR = "baz";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private Instrumentation.Context.Default.AuxiliaryTypeNamingStrategy auxiliaryTypeNamingStrategy;
    @Mock
    private AuxiliaryType.MethodAccessorFactory methodAccessorFactory;
    @Mock
    private AuxiliaryType firstAuxiliary, secondAuxiliary;
    @Mock
    private DynamicType firstDynamic, secondDynamic;
    @Mock
    private TypeDescription firstDescription, secondDescription;
    @Mock
    private MethodDescription firstMethod, firstProxyMethod, secondMethod, secondProxyMethod;
    @Mock
    private ClassFileVersion classFileVersion;

    private Instrumentation.Context.Default defaultContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        defaultContext = new Instrumentation.Context.Default(classFileVersion, auxiliaryTypeNamingStrategy, methodAccessorFactory);
        when(firstAuxiliary.make(any(String.class), any(ClassFileVersion.class), any(AuxiliaryType.MethodAccessorFactory.class)))
                .thenReturn(firstDynamic);
        when(secondAuxiliary.make(any(String.class), any(ClassFileVersion.class), any(AuxiliaryType.MethodAccessorFactory.class)))
                .thenReturn(secondDynamic);
        when(firstDynamic.getDescription()).thenReturn(firstDescription);
        when(secondDynamic.getDescription()).thenReturn(secondDescription);
        when(auxiliaryTypeNamingStrategy.name(any(AuxiliaryType.class))).thenReturn(FOO, BAR);
        when(methodAccessorFactory.requireAccessorMethodFor(firstMethod)).thenReturn(firstProxyMethod);
        when(methodAccessorFactory.requireAccessorMethodFor(secondMethod)).thenReturn(secondProxyMethod);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleRegistration() throws Exception {
        assertThat(defaultContext.register(firstAuxiliary), is(firstDescription));
        assertThat(defaultContext.register(secondAuxiliary), is(secondDescription));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes().size(), is(2));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes(), hasItems(firstDynamic, secondDynamic));
        verify(firstAuxiliary).make(FOO, classFileVersion, defaultContext);
        verify(secondAuxiliary).make(BAR, classFileVersion, defaultContext);
        verifyNoMoreInteractions(firstAuxiliary);
        verifyNoMoreInteractions(secondAuxiliary);
        verify(auxiliaryTypeNamingStrategy).name(firstAuxiliary);
        verify(auxiliaryTypeNamingStrategy).name(secondAuxiliary);
        verifyNoMoreInteractions(auxiliaryTypeNamingStrategy);
        verifyZeroInteractions(methodAccessorFactory);
        verifyZeroInteractions(classFileVersion);
    }

    @Test
    public void testDoubleRegistration() throws Exception {
        assertThat(defaultContext.register(firstAuxiliary), is(firstDescription));
        assertThat(defaultContext.register(firstAuxiliary), is(firstDescription));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes().size(), is(1));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes(), hasItem(firstDynamic));
        verify(firstAuxiliary).make(FOO, classFileVersion, defaultContext);
        verifyNoMoreInteractions(firstAuxiliary);
        verify(auxiliaryTypeNamingStrategy).name(firstAuxiliary);
        verifyNoMoreInteractions(auxiliaryTypeNamingStrategy);
        verifyZeroInteractions(methodAccessorFactory);
        verifyZeroInteractions(classFileVersion);
    }

    @Test
    public void testMethodProxyFactory() throws Exception {
        assertThat(defaultContext.requireAccessorMethodFor(firstMethod), is(firstProxyMethod));
        assertThat(defaultContext.requireAccessorMethodFor(secondMethod), is(secondProxyMethod));
        assertThat(defaultContext.requireAccessorMethodFor(firstMethod), is(firstProxyMethod));
        verify(methodAccessorFactory).requireAccessorMethodFor(firstMethod);
        verify(methodAccessorFactory).requireAccessorMethodFor(secondMethod);
        verifyNoMoreInteractions(methodAccessorFactory);
        verifyZeroInteractions(auxiliaryTypeNamingStrategy);
        verifyZeroInteractions(classFileVersion);
    }
}
