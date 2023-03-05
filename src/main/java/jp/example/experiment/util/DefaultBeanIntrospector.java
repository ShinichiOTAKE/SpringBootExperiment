package jp.example.experiment.util;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultBeanIntrospector implements BeanIntrospector {
	
	private static final Class<?>[] LIST_CLASS_PARAMETER = new Class[] { java.util.List.class };
	
	static final PropertyDescriptor[] EMPTY_ARRAY = {};
	static final Class<?>[] EMPTY_CLASS_ARRAY = {};

    @Override
    public void introspect(final IntrospectionContext icontext) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(icontext.getTargetClass());
        } catch (final IntrospectionException e) {
        	;
        }

        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        if (descriptors == null) {
            descriptors = EMPTY_ARRAY;
        }

        handleIndexedPropertyDescriptors(icontext.getTargetClass(),
                descriptors);
        icontext.addPropertyDescriptors(descriptors);
    }
    
    
    
    private void handleIndexedPropertyDescriptors(final Class<?> beanClass, final PropertyDescriptor[] descriptors) {
        for (final PropertyDescriptor pd : descriptors) {
            if (pd instanceof IndexedPropertyDescriptor) {
                final IndexedPropertyDescriptor descriptor = (IndexedPropertyDescriptor) pd;
                final String propName = descriptor.getName().substring(0, 1)
                        .toUpperCase()
                        + descriptor.getName().substring(1);

                if (descriptor.getReadMethod() == null) {
                    final String methodName = descriptor.getIndexedReadMethod() != null ? descriptor
                            .getIndexedReadMethod().getName() : "get"
                            + propName;
                    final Method readMethod = MethodUtils.getMatchingAccessibleMethod(beanClass, methodName, EMPTY_CLASS_ARRAY);
                    if (readMethod != null) {
                        try {
                            descriptor.setReadMethod(readMethod);
                        } catch (final Exception e) {
                            ;
                        }
                    }
                }
                if (descriptor.getWriteMethod() == null) {
                    final String methodName = descriptor.getIndexedWriteMethod() != null ? descriptor
                            .getIndexedWriteMethod().getName() : "set"
                            + propName;
                    Method writeMethod = MethodUtils
                            .getMatchingAccessibleMethod(beanClass, methodName,
                                    LIST_CLASS_PARAMETER);
                    if (writeMethod == null) {
                        for (final Method m : beanClass.getMethods()) {
                            if (m.getName().equals(methodName)) {
                                final Class<?>[] parameterTypes = m.getParameterTypes();
                                if (parameterTypes.length == 1
                                        && List.class
                                                .isAssignableFrom(parameterTypes[0])) {
                                    writeMethod = m;
                                    break;
                                }
                            }
                        }
                    }
                    if (writeMethod != null) {
                        try {
                            descriptor.setWriteMethod(writeMethod);
                        } catch (final Exception e) {
                            ;
                        }
                    }
                }
            }
        }
    }
	
}
