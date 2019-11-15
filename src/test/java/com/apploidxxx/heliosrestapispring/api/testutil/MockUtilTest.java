package com.apploidxxx.heliosrestapispring.api.testutil;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

/**
 * @author Arthur Kupriyanov
 */

public class MockUtilTest {
    private MockUtil mockUtil = new MockUtil();

    @Test
    public void test_is_annotated_userRepository() throws NoSuchFieldException {
        Field field = mockUtil.getClass().getDeclaredField("userRepository");
        boolean mockBeanAnnotationFound = false;

        for (Annotation a : field.getAnnotations()){

            if (a.annotationType().getName().equals(MockBean.class.getName())){
                mockBeanAnnotationFound = true;
                break;
            }
        }

        assertTrue(mockBeanAnnotationFound);
    }

    @Test
    public void is_mockUtil_is_component(){

        boolean annotated = false;

        for (Annotation a : mockUtil.getClass().getDeclaredAnnotations()){
            if (a.annotationType().getName().equals(Component.class.getName())){
                annotated = true;
                break;
            }
        }

        assertTrue(annotated);
    }
}