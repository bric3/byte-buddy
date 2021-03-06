package net.bytebuddy.dynamic.scaffold;

import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.MethodList;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.utility.MockitoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static net.bytebuddy.instrumentation.method.matcher.MethodMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BridgeMethodResolverSimpleTest {

    private static final String FOO = "foo";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private BridgeMethodResolver.Simple.ConflictHandler conflictHandler;
    @Mock
    private BridgeMethodResolver.Simple.BridgeTarget bridgeTarget;
    @Mock
    private MethodDescription methodDescription;

    @Test
    public void testFindsBridgeMethodSingleStep() throws Exception {
        TypeDescription target = new TypeDescription.ForLoadedType(Bar.class);
        MethodList relevantMethods = target.getReachableMethods().filter(not(isConstructor().or(isDeclaredBy(Object.class))));
        assertThat(relevantMethods.size(), is(2));
        BridgeMethodResolver bridgeMethodResolver = new BridgeMethodResolver.Simple(target.getReachableMethods(), conflictHandler);
        assertThat(bridgeMethodResolver.resolve(relevantMethods.filter(isBridge()).getOnly()),
                is(relevantMethods.filter(not(isBridge())).getOnly()));
        verifyZeroInteractions(conflictHandler);
    }

    @Test
    public void testFindsBridgeMethodTwoStep() throws Exception {
        TypeDescription target = new TypeDescription.ForLoadedType(Qux.class);
        MethodList relevantMethods = target.getReachableMethods().filter(not(isConstructor().or(isDeclaredBy(Object.class))));
        assertThat(relevantMethods.size(), is(3));
        BridgeMethodResolver bridgeMethodResolver = new BridgeMethodResolver.Simple(target.getReachableMethods(), conflictHandler);
        for (MethodDescription methodDescription : relevantMethods.filter(isBridge())) {
            assertThat(bridgeMethodResolver.resolve(methodDescription), is(relevantMethods.filter(not(isBridge())).getOnly()));
        }
        verifyZeroInteractions(conflictHandler);
    }

    @Test
    public void testFindsBridgeMethodConflictResolver() throws Exception {
        TypeDescription target = new TypeDescription.ForLoadedType(Baz.class);
        MethodList relevantMethods = target.getReachableMethods().filter(not(isConstructor().or(isDeclaredBy(Object.class))));
        assertThat(relevantMethods.size(), is(3));
        when(conflictHandler.choose(any(MethodDescription.class), any(MethodList.class))).thenReturn(bridgeTarget);
        when(bridgeTarget.isResolved()).thenReturn(true);
        when(bridgeTarget.extract()).thenReturn(methodDescription);
        BridgeMethodResolver bridgeMethodResolver = new BridgeMethodResolver.Simple(target.getReachableMethods(), conflictHandler);
        assertThat(bridgeMethodResolver.resolve(relevantMethods.filter(isBridge()).getOnly()), is(methodDescription));
        verify(conflictHandler).choose(relevantMethods.filter(isBridge()).getOnly(), relevantMethods.filter(not(isBridge())));
        verifyNoMoreInteractions(conflictHandler);
        verify(bridgeTarget).isResolved();
        verify(bridgeTarget).extract();
        verifyNoMoreInteractions(bridgeTarget);
    }

    private static class Foo<T> {

        public T foo(T t) {
            return null;
        }
    }

    private static class Bar<T extends Number> extends Foo<T> {

        @Override
        public T foo(T t) {
            return null;
        }
    }

    private static class Qux extends Bar<Integer> {

        @Override
        public Integer foo(Integer integer) {
            return null;
        }
    }

    private static class Baz extends Foo<Integer> {

        @Override
        public Integer foo(Integer i) {
            return null;
        }

        public String foo(String s) {
            return null;
        }
    }
}
