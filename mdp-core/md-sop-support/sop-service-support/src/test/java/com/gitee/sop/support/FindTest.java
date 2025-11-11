package com.gitee.sop.support;

import junit.framework.TestCase;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author 六如
 */
public class FindTest extends TestCase {

    @Test
    public void testFind() {
        ServiceImpl service = new ServiceImpl();
        Class<?> aClass = service.getClass();
        for (Method method : aClass.getMethods()) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                continue;
            }
            //System.out.println(method);
//            System.out.println(method.getDeclaringClass().isInterface());
            for (Class<?> anInterface : declaringClass.getInterfaces()) {
                try {
                    Method parentMethod = anInterface.getMethod(method.getName(), method.getParameterTypes());
                    if (method.getName().equals(parentMethod.getName())) {
                        System.out.println("same:" + method);
                    }
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
        }
    }

    interface Service {
        void h();

        String say();

        void hello(int i);

        String hello2(int i, List list);

        String hello3(int i, int... j);

    }

    static class ServiceImpl implements Service {
        @Override
        public void h() {

        }

        @Override
        public String say() {
            return "";
        }

        @Override
        public void hello(int i) {

        }

        @Override
        public String hello2(int i, List list) {
            return "";
        }

        @Override
        public String hello3(int i, int... j) {
            return "";
        }

        public int add() {
            return 0;
        }
    }


}
