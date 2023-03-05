package jp.example.experiment.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MethodUtils { 
	
	private static boolean CACHE_METHODS = true;
    private static final Map<MethodDescriptor, Reference<Method>> cache = 
    		Collections.synchronizedMap(new WeakHashMap<MethodDescriptor, Reference<Method>>());
	

    public static Method getAccessibleMethod(Class<?> clazz, Method method) {
    	
        if (method == null) {
            return null;
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        boolean sameClass = true;
        if (clazz == null) {
            clazz = method.getDeclaringClass();
        } else {
            if (!method.getDeclaringClass().isAssignableFrom(clazz)) {
                throw new IllegalArgumentException();
            }
            sameClass = clazz.equals(method.getDeclaringClass());
        }

        if (Modifier.isPublic(clazz.getModifiers())) {
            if (!sameClass && !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                setMethodAccessible(method);
            }
            return method;
        }

        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();

        method = getAccessibleMethodFromInterfaceNest(clazz, methodName, parameterTypes);

        if (method == null) {
            method = getAccessibleMethodFromSuperclass(clazz, methodName, parameterTypes);
        }

        return method;
    }
    
    
    
    public static Method getAccessibleMethod(final Class<?> clazz, final String methodName, final Class<?> parameterType) {
        final Class<?>[] parameterTypes = {parameterType};
        return getAccessibleMethod(clazz, methodName, parameterTypes);
    }
    
    
    
    public static Method getAccessibleMethod(final Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {
        try {
            final MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, true);
            
            Method method = getCachedMethod(md);
            if (method != null) {
                return method;
            }

            method =  getAccessibleMethod
                    (clazz, clazz.getMethod(methodName, parameterTypes));
            cacheMethod(md, method);
            return method;
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }
    
    
    
    public static Method getMatchingAccessibleMethod(final Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {

    	final MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, false);

    	try {
    		Method method = getCachedMethod(md);
    		if (method != null) {
    			return method;
    		}

    		method = clazz.getMethod(methodName, parameterTypes);

    		setMethodAccessible(method); // Default access superclass workaround

    		cacheMethod(md, method);
    		return method;

    	} catch (final NoSuchMethodException e) {
    		;
    	}

    	final int paramSize = parameterTypes.length;
    	Method bestMatch = null;
    	final Method[] methods = clazz.getMethods();
    	float bestMatchCost = Float.MAX_VALUE;
    	float myCost = Float.MAX_VALUE;
    	for (final Method method2 : methods) {
    		if (method2.getName().equals(methodName)) {
    			final Class<?>[] methodsParams = method2.getParameterTypes();
    			final int methodParamSize = methodsParams.length;
    			if (methodParamSize == paramSize) {
    				boolean match = true;
    				for (int n = 0 ; n < methodParamSize; n++) {
    					if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
    						match = false;
    						break;
    					}
    				}

    				if (match) {
    					final Method method = getAccessibleMethod(clazz, method2);
    					if (method != null) {
    						setMethodAccessible(method);
    						myCost = getTotalTransformationCost(parameterTypes,method.getParameterTypes());
    						if ( myCost < bestMatchCost ) {
    							bestMatch = method;
    							bestMatchCost = myCost;
    						}
    					}
    				}
    			}
    		}
    	}

    	if (bestMatch != null) {
    		cacheMethod(md, bestMatch);
    	}

    	return bestMatch;
    }
    
    

    private static void setMethodAccessible(final Method method) {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
    }
    
    
    
    private static Method getAccessibleMethodFromInterfaceNest(Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {
    	Method method = null;

    	for (; clazz != null; clazz = clazz.getSuperclass()) {

	    	final Class<?>[] interfaces = clazz.getInterfaces();
	    	for (final Class<?> anInterface : interfaces) {
		        if (!Modifier.isPublic(anInterface.getModifiers())) {
		            continue;
		        }
	
		        try {
		        	method = anInterface.getDeclaredMethod(methodName, parameterTypes);
		        } catch (final NoSuchMethodException e) {
		            ;
		        }
		        
		        if (method != null) {
		        	return method;
		        }
	
		        method = getAccessibleMethodFromInterfaceNest(anInterface, methodName, parameterTypes);
		        if (method != null) {
		        	return method;
		        }
	    	}
    	}
    	
    	return null;
    }

    
    
    private static Method getAccessibleMethodFromSuperclass(final Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {

    	Class<?> parentClazz = clazz.getSuperclass();
    	while (parentClazz != null) {
    		if (Modifier.isPublic(parentClazz.getModifiers())) {
    			try {
    				return parentClazz.getMethod(methodName, parameterTypes);
    			} catch (final NoSuchMethodException e) {
    				return null;
    			}
    		}
    		parentClazz = parentClazz.getSuperclass();
    	}
    	
    	return null;
    }
    
    
    
    private static Method getCachedMethod(final MethodDescriptor md) {
        if (CACHE_METHODS) {
            final Reference<Method> methodRef = cache.get(md);
            if (methodRef != null) {
                return methodRef.get();
            }
        }
        return null;
    }
    
    
    
    private static void cacheMethod(final MethodDescriptor md, final Method method) {
        if (CACHE_METHODS && method != null) {
            cache.put(md, new WeakReference<>(method));
        }
    }
    
    
    
    public static final boolean isAssignmentCompatible(final Class<?> parameterType, final Class<?> parameterization) {
        // try plain assignment
        if (parameterType.isAssignableFrom(parameterization)) {
            return true;
        }

        if (parameterType.isPrimitive()) {
            // this method does *not* do widening - you must specify exactly
            // is this the right behavior?
            final Class<?> parameterWrapperClazz = getPrimitiveWrapper(parameterType);
            if (parameterWrapperClazz != null) {
                return parameterWrapperClazz.equals(parameterization);
            }
        }

        return false;
    }
    
    
    
    private static float getTotalTransformationCost(final Class<?>[] srcArgs, final Class<?>[] destArgs) {
        float totalCost = 0.0f;
        for (int i = 0; i < srcArgs.length; i++) {
            Class<?> srcClass, destClass;
            srcClass = srcArgs[i];
            destClass = destArgs[i];
            totalCost += getObjectTransformationCost(srcClass, destClass);
        }

        return totalCost;
    }

    
    
    public static Class<?> getPrimitiveWrapper(final Class<?> primitiveType) {

        if (boolean.class.equals(primitiveType)) {
            return Boolean.class;
        }
        if (float.class.equals(primitiveType)) {
            return Float.class;
        }
        if (long.class.equals(primitiveType)) {
            return Long.class;
        }
        if (int.class.equals(primitiveType)) {
            return Integer.class;
        }
        if (short.class.equals(primitiveType)) {
            return Short.class;
        }
        if (byte.class.equals(primitiveType)) {
            return Byte.class;
        }
        if (double.class.equals(primitiveType)) {
            return Double.class;
        }
        if (char.class.equals(primitiveType)) {
            return Character.class;
        }
        
        return null;
    }
    
    
    
    private static float getObjectTransformationCost(Class<?> srcClass, final Class<?> destClass) {
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isPrimitive()) {
                final Class<?> destClassWrapperClazz = getPrimitiveWrapper(destClass);
                if (destClassWrapperClazz != null && destClassWrapperClazz.equals(srcClass)) {
                    cost += 0.25f;
                    break;
                }
            }
            if (destClass.isInterface() && isAssignmentCompatible(destClass,srcClass)) {
                cost += 0.25f;
                break;
            }
            cost++;
            srcClass = srcClass.getSuperclass();
        }

        if (srcClass == null) {
            cost += 1.5f;
        }

        return cost;
    }
    
    
    
    private static class MethodDescriptor {
    	
    	
        static final Class<?>[] EMPTY_CLASS_ARRAY = {};
        
        
    	
        private final Class<?> cls;
        private final String methodName;
        private final Class<?>[] paramTypes;
        private final boolean exact;
        private final int hashCode;

        
        
        public MethodDescriptor(final Class<?> cls, final String methodName, Class<?>[] paramTypes, final boolean exact) {
            if (cls == null) {
                throw new IllegalArgumentException("Class cannot be null");
            }
            if (methodName == null) {
                throw new IllegalArgumentException("Method Name cannot be null");
            }
            if (paramTypes == null) {
                paramTypes = EMPTY_CLASS_ARRAY;
            }

            this.cls = cls;
            this.methodName = methodName;
            this.paramTypes = paramTypes;
            this.exact= exact;

            this.hashCode = methodName.length();
        }

        
        
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof MethodDescriptor)) {
                return false;
            }
            final MethodDescriptor md = (MethodDescriptor)obj;

            return exact == md.exact &&
            methodName.equals(md.methodName) &&
            cls.equals(md.cls) &&
            java.util.Arrays.equals(paramTypes, md.paramTypes);
        }

        
        
        @Override
        public int hashCode() {
            return hashCode;
        }
    }
	
}
