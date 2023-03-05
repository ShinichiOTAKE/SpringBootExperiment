package jp.example.experiment.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class BeanIntrospectionData {
	
	private final PropertyDescriptor[] descriptors;
	private final Map<String, String> writeMethodNames;
	
	
	
    public BeanIntrospectionData(final PropertyDescriptor[] descs) {
        this(descs, setUpWriteMethodNames(descs));
    }
    
    
    
    BeanIntrospectionData(final PropertyDescriptor[] descs, final Map<String, String> writeMethNames) {
        descriptors = descs;
        writeMethodNames = writeMethNames;
    }

	
	
    public PropertyDescriptor getDescriptor(final String name) {
        for (final PropertyDescriptor pd : getDescriptors()) {
            if (name.equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }
    
    
    
    public PropertyDescriptor[] getDescriptors() {
        return descriptors;
    }
    
    
    
    public Method getWriteMethod(final Class<?> beanCls, final PropertyDescriptor desc) {
        Method method = desc.getWriteMethod();
        if (method == null) {
            final String methodName = writeMethodNames.get(desc.getName());
            if (methodName != null) {
                method = MethodUtils.getAccessibleMethod(beanCls, methodName,
                        desc.getPropertyType());
                if (method != null) {
                    try {
                        desc.setWriteMethod(method);
                    } catch (final IntrospectionException e) {
                        ;
                    }
                }
            }
        }

        return method;
    }
    
    
    
    private static Map<String, String> setUpWriteMethodNames(final PropertyDescriptor[] descs) {
        final Map<String, String> methods = new HashMap<>();
        for (final PropertyDescriptor pd : descs) {
            final Method method = pd.getWriteMethod();
            if (method != null) {
                methods.put(pd.getName(), method.getName());
            }
        }
        return methods;
    }
}
