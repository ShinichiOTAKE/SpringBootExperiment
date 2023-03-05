package jp.example.experiment.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MappedPropertyDescriptor extends PropertyDescriptor {
	
	private static final Class<?>[] STRING_CLASS_PARAMETER = new Class[]{String.class};
	
	
	
	private MappedMethodReference mappedReadMethodRef;
	private MappedMethodReference mappedWriteMethodRef;
	private Reference<Class<?>> mappedPropertyTypeRef;
	
	

    public MappedPropertyDescriptor(final String propertyName, final Class<?> beanClass)
            throws IntrospectionException {
        super(propertyName, null, null);

        if (propertyName == null || propertyName.isEmpty()) {
        	String className = beanClass.getClass().getName();
            throw new IntrospectionException("Class: " + className + ", Property: " + propertyName);
        }

        setName(propertyName);
        final String base = capitalizePropertyName(propertyName);

        Method mappedReadMethod = null;
        Method mappedWriteMethod = null;
        try {
            try {
                mappedReadMethod = getMethod(beanClass, "get" + base,
                        STRING_CLASS_PARAMETER);
            } catch (final IntrospectionException e) {
                mappedReadMethod = getMethod(beanClass, "is" + base,
                        STRING_CLASS_PARAMETER);
            }
            final Class<?>[] params = { String.class, mappedReadMethod.getReturnType() };
            mappedWriteMethod = getMethod(beanClass, "set" + base, params);
        } catch (final IntrospectionException e) {
            ;
        }

        if (mappedReadMethod == null) {
            mappedWriteMethod = getMethod(beanClass, "set" + base, 2);
        }

        if (mappedReadMethod == null && mappedWriteMethod == null) {
            throw new IntrospectionException("Property '" + propertyName +
                    "' not found on " +
                    beanClass.getName());
        }
        mappedReadMethodRef  = new MappedMethodReference(mappedReadMethod);
        mappedWriteMethodRef = new MappedMethodReference(mappedWriteMethod);

        findMappedPropertyType();
    }
	
	
	
    public Method getMappedReadMethod() {
        return mappedReadMethodRef.get();
    }
	
    
    
    public Method getMappedWriteMethod() {
        return mappedWriteMethodRef.get();
    }
    
    
    
    private static String capitalizePropertyName(final String s) {
        if (s.isEmpty()) {
            return s;
        }

        final char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    
    
    private static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>[] parameterTypes)
            throws IntrospectionException {

    	if (methodName == null) {
    		return null;
    	}

    	final Method method = MethodUtils.getMatchingAccessibleMethod(clazz, methodName, parameterTypes);
    	if (method != null) {
    		return method;
    	}

    	final int parameterCount = parameterTypes == null ? 0 : parameterTypes.length;

    	throw new IntrospectionException("No such method: " + methodName);
    }
    
    
    
    private static Method getMethod(final Class<?> clazz, final String methodName, final int parameterCount)
            throws IntrospectionException {
    	
        if (methodName == null) {
            return null;
        }

        final Method method = internalGetMethod(clazz, methodName, parameterCount);
        if (method != null) {
            return method;
        }

        throw new IntrospectionException("No such method: " + methodName);
    }
    
    
    
    private void findMappedPropertyType() throws IntrospectionException {
        final Method mappedReadMethod  = getMappedReadMethod();
        final Method mappedWriteMethod = getMappedWriteMethod();
        Class<?> mappedPropertyType = null;
        if (mappedReadMethod != null) {
            if (mappedReadMethod.getParameterTypes().length != 1) {
                throw new IntrospectionException
                        ("bad mapped read method arg count");
            }
            mappedPropertyType = mappedReadMethod.getReturnType();
            if (mappedPropertyType == Void.TYPE) {
                throw new IntrospectionException
                        ("mapped read method " +
                        mappedReadMethod.getName() + " returns void");
            }
        }

        if (mappedWriteMethod != null) {
            final Class<?>[] params = mappedWriteMethod.getParameterTypes();
            if (params.length != 2) {
                throw new IntrospectionException
                        ("bad mapped write method arg count");
            }
            if (mappedPropertyType != null &&
                    mappedPropertyType != params[1]) {
                throw new IntrospectionException
                        ("type mismatch between mapped read and write methods");
            }
            mappedPropertyType = params[1];
        }
        mappedPropertyTypeRef = new SoftReference<>(mappedPropertyType);
    }
    
    
    
    private static Method internalGetMethod(final Class<?> initial, final String methodName,
            final int parameterCount) {

    	for (Class<?> clazz = initial; clazz != null; clazz = clazz.getSuperclass()) {
    		final Method[] methods = clazz.getDeclaredMethods();
    		for (final Method method : methods) {
    			if (method == null) {
    				continue;
    			}

    			final int mods = method.getModifiers();
    			if (!Modifier.isPublic(mods) || Modifier.isStatic(mods)) {
    				continue;
    			}
    			if (method.getName().equals(methodName) && method.getParameterTypes().length == parameterCount) {
    				return method;
    			}
    		}
    	}

    	final Class<?>[] interfaces = initial.getInterfaces();
    	for (final Class<?> interface1 : interfaces) {
    		final Method method = internalGetMethod(interface1, methodName, parameterCount);
    		if (method != null) {
    			return method;
    		}
    	}
  
    	return null;
    }
    
    
    
    private static class MappedMethodReference {
        private String className;
        private String methodName;
        private Reference<Method> methodRef;
        private Reference<Class<?>> classRef;
        private Reference<Class<?>> writeParamTypeRef0;
        private Reference<Class<?>> writeParamTypeRef1;
        private String[] writeParamClassNames;
        MappedMethodReference(final Method m) {
            if (m != null) {
                className = m.getDeclaringClass().getName();
                methodName = m.getName();
                methodRef = new SoftReference<>(m);
                classRef = new WeakReference<>(m.getDeclaringClass());
                final Class<?>[] types = m.getParameterTypes();
                if (types.length == 2) {
                    writeParamTypeRef0 = new WeakReference<>(types[0]);
                    writeParamTypeRef1 = new WeakReference<>(types[1]);
                    writeParamClassNames = new String[2];
                    writeParamClassNames[0] = types[0].getName();
                    writeParamClassNames[1] = types[1].getName();
                }
            }
        }
        private Method get() {
            if (methodRef == null) {
                return null;
            }
            Method m = methodRef.get();
            if (m == null) {
                Class<?> clazz = classRef.get();
                if (clazz == null) {
                    clazz = reLoadClass();
                    if (clazz != null) {
                        classRef = new WeakReference<>(clazz);
                    }
                }
                if (clazz == null) {
                    throw new RuntimeException("Method " + methodName + " for " +
                            className + " could not be reconstructed - class reference has gone");
                }
                Class<?>[] paramTypes = null;
                if (writeParamClassNames != null) {
                    paramTypes = new Class[2];
                    paramTypes[0] = writeParamTypeRef0.get();
                    if (paramTypes[0] == null) {
                        paramTypes[0] = reLoadClass(writeParamClassNames[0]);
                        if (paramTypes[0] != null) {
                            writeParamTypeRef0 = new WeakReference<>(paramTypes[0]);
                        }
                    }
                    paramTypes[1] = writeParamTypeRef1.get();
                    if (paramTypes[1] == null) {
                        paramTypes[1] = reLoadClass(writeParamClassNames[1]);
                        if (paramTypes[1] != null) {
                            writeParamTypeRef1 = new WeakReference<>(paramTypes[1]);
                        }
                    }
                } else {
                    paramTypes = STRING_CLASS_PARAMETER;
                }
                try {
                    m = clazz.getMethod(methodName, paramTypes);
                    // Un-comment following line for testing
                    // System.out.println("Recreated Method " + methodName + " for " + className);
                } catch (final NoSuchMethodException e) {
                    throw new RuntimeException("Method " + methodName + " for " +
                            className + " could not be reconstructed - method not found");
                }
                methodRef = new SoftReference<>(m);
            }
            return m;
        }

        /**
         * Try to re-load the class
         */
        private Class<?> reLoadClass() {
            return reLoadClass(className);
        }

        /**
         * Try to re-load the class
         */
        private Class<?> reLoadClass(final String name) {

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // Try the context class loader
            if (classLoader != null) {
                try {
                    return classLoader.loadClass(name);
                } catch (final ClassNotFoundException e) {
                    // ignore
                }
            }

            // Try this class's class loader
            classLoader = MappedPropertyDescriptor.class.getClassLoader();
            try {
                return classLoader.loadClass(name);
            } catch (final ClassNotFoundException e) {
                return null;
            }
        }
    }
}
