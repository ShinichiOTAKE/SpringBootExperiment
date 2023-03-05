package jp.example.experiment.util;

import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PropertyUtilsBean {
	
    static final Object[] EMPTY_OBJECT_ARRAY = {};
    
    
    
    private final Map<Class<?>, BeanIntrospectionData> descriptorsCache;
    private final Map<Class<?>, Map> mappedDescriptorsCache;
    private final List<BeanIntrospector> introspectors;
    private Resolver resolver = new Resolver();
	
    
    
    public PropertyUtilsBean() {
    	this.descriptorsCache = new HashMap<>();
    	this.mappedDescriptorsCache = new HashMap<>();
    	this.introspectors = new CopyOnWriteArrayList<>();
    }
    
    
    public Resolver getResolver() {
      	return resolver;
    }
	
    
    
	public PropertyDescriptor[] getPropertyDescriptors(final Object bean) throws IntrospectionException {
		if (bean == null) {
			throw new IllegalArgumentException();
		}
		
		return getPropertyDescriptors(bean.getClass());
	}
	
	
	
	public PropertyDescriptor[] getPropertyDescriptors(final Class<?> beanClass) throws IntrospectionException {
		return getIntrospectionData(beanClass).getDescriptors();	
	}
	
	
	
	public boolean isReadable(Object bean, String name) 
		throws NestedNullException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
		
		if (bean == null || name == null) {
			throw new IllegalArgumentException();
		}
		
		while (resolver.hasNested(name)) {
			final String next = resolver.next(name);
			
			Object nestedBean = null;
			try {
				
				nestedBean = getProperty(bean, next);
			}
			catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				return false;
			}
			
			if (nestedBean == null) {
				throw new NestedNullException();
			}
			
            bean = nestedBean;
            name = resolver.remove(name);			
		}
		
        name = resolver.getProperty(name);

        final PropertyDescriptor desc = getPropertyDescriptor(bean, name);
		if (desc != null) {	
			Method readMethod = getReadMethod(bean.getClass(), desc);
		    if (readMethod == null) {
		        if (desc instanceof IndexedPropertyDescriptor) {
		            readMethod = ((IndexedPropertyDescriptor) desc).getIndexedReadMethod();
		        } else if (desc instanceof MappedPropertyDescriptor) {
		            readMethod = ((MappedPropertyDescriptor) desc).getMappedReadMethod();
		        }
		        readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
		    }
		    return readMethod != null;
		}
		return false;
    }
	
	
	
    public boolean isWriteable(Object bean, String name)
    	throws NestedNullException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
        
        if (bean == null || name == null) {
            throw new IllegalArgumentException();
        }

        while (resolver.hasNested(name)) {
            final String next = resolver.next(name);
            Object nestedBean = null;
            try {
                nestedBean = getProperty(bean, next);
            } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                return false;
            }
            if (nestedBean == null) {
                throw new NestedNullException();
            }
            bean = nestedBean;
            name = resolver.remove(name);
        }

        name = resolver.getProperty(name);

        final PropertyDescriptor desc = getPropertyDescriptor(bean, name);
		if (desc != null) {
		    Method writeMethod = getWriteMethod(bean.getClass(), desc);
		    if (writeMethod == null) {
		        if (desc instanceof IndexedPropertyDescriptor) {
		            writeMethod = ((IndexedPropertyDescriptor) desc).getIndexedWriteMethod();
		        } else if (desc instanceof MappedPropertyDescriptor) {
		            writeMethod = ((MappedPropertyDescriptor) desc).getMappedWriteMethod();
		        }
		        writeMethod = MethodUtils.getAccessibleMethod(bean.getClass(), writeMethod);
		    }
		    return writeMethod != null;
		}
		return false;
    }
    
 
    
    public Object getSimpleProperty(final Object bean, final String name)
    		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NestedNullException, IntrospectionException {
    	
        if (bean == null || name == null) {
            throw new IllegalArgumentException();
        }

        if (resolver.hasNested(name)) {
            throw new IllegalArgumentException
                    ("Nested property names are not allowed: Property '" +
                    name + "' on bean class '" + bean.getClass() + "'");
        }
        if (resolver.isIndexed(name)) {
            throw new IllegalArgumentException
                    ("Indexed property names are not allowed: Property '" +
                    name + "' on bean class '" + bean.getClass() + "'");
        }
        if (resolver.isMapped(name)) {
            throw new IllegalArgumentException
                    ("Mapped property names are not allowed: Property '" +
                    name + "' on bean class '" + bean.getClass() + "'");
        }

        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor == null) {
            throw new NoSuchMethodException("Unknown property '" +
                    name + "' on class '" + bean.getClass() + "'" );
        }
        final Method readMethod = getReadMethod(bean.getClass(), descriptor);
        if (readMethod == null) {
            throw new NoSuchMethodException("Property '" + name +
                    "' has no getter method in class '" + bean.getClass() + "'");
        }

        return invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
    }
	
    
    
    public PropertyDescriptor getPropertyDescriptor(Object bean, String name) 
    		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
    	
    	if (bean == null || name == null) {
    		throw new IllegalArgumentException();
    	}
    	
    	while (resolver.hasNested(name)) {
    		final String next = resolver.next(name);
    		final Object nestedBean = getProperty(bean, next);
    		if (nestedBean == null) {
    			throw new NestedNullException();
    		}
    		bean = nestedBean;
    		name = resolver.remove(name);
    	}
 
    	name = resolver.getProperty(name);

    	if (name == null) {
    		return null;
    	}

    	final BeanIntrospectionData data = getIntrospectionData(bean.getClass());
    	PropertyDescriptor result = data.getDescriptor(name);
    	if (result != null) {
    		return result;
    	}

    	Map mappedDescriptors = getMappedPropertyDescriptors(bean);
    	if (mappedDescriptors == null) {
    		mappedDescriptors = new ConcurrentHashMap<Class<?>, Map>();
    		mappedDescriptorsCache.put(bean.getClass(), mappedDescriptors);
    	}
    	result = (PropertyDescriptor) mappedDescriptors.get(name);
    	if (result == null) {
    		try {
    			result = new MappedPropertyDescriptor(name, bean.getClass());
    		} catch (final IntrospectionException ie) {
    			;
    		}
    		if (result != null) {
    			mappedDescriptors.put(name, result);
    		}
    	}

    	return result;
    }
    
    
    
    public void setIndexedProperty(final Object bean, final String name, final int index, final Object value) 
    		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {

    	if (bean == null) {
    		throw new IllegalArgumentException();
    	}
    	if (name == null || name.isEmpty()) {
    		if (bean.getClass().isArray()) {
    			Array.set(bean, index, value);
    			return;
    		}
    		if (bean instanceof List) {
    			final List<Object> list = toObjectList(bean);
    			list.set(index, value);
    			return;
    		}
    	}
    	
    	if (name == null) {
    		throw new IllegalArgumentException();
    	}

    	final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
    	if (descriptor == null) {
    		throw new NoSuchMethodException();
    	}

    	if (descriptor instanceof IndexedPropertyDescriptor) {
    		Method writeMethod = ((IndexedPropertyDescriptor) descriptor).getIndexedWriteMethod();
    		writeMethod = MethodUtils.getAccessibleMethod(bean.getClass(), writeMethod);
    		if (writeMethod != null) {
    			final Object[] subscript = new Object[2];
    			subscript[0] = Integer.valueOf(index);
    			subscript[1] = value;
    			try {
    				invokeMethod(writeMethod, bean, subscript);
    			} catch (final InvocationTargetException e) {
    				if (e.getTargetException() instanceof IndexOutOfBoundsException) {
    					throw (IndexOutOfBoundsException)
    					e.getTargetException();
    				}
    				throw e;
    			}
    			return;
    		}
    	}

    	final Method readMethod = getReadMethod(bean.getClass(), descriptor);
    	if (readMethod == null) {
    		throw new NoSuchMethodException();
    	}

    	final Object array = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
    	if (!array.getClass().isArray()) {
    		if (!(array instanceof List)) {
    			throw new IllegalArgumentException();
    		}
    		final List<Object> list = toObjectList(array);
    		list.set(index, value);
    	} else {
    		Array.set(array, index, value);
    	}
    }
    
    
    
    public void setMappedProperty(final Object bean, final String name, final String key, final Object value) 
    		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
    	if (bean == null || name == null || key == null) {
    		throw new IllegalArgumentException("No bean specified");
    	}

    	final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
    	if (descriptor == null) {
    		throw new NoSuchMethodException();
    	}

    	if (descriptor instanceof MappedPropertyDescriptor) {
    		Method mappedWriteMethod = ((MappedPropertyDescriptor) descriptor).getMappedWriteMethod();
    		mappedWriteMethod = MethodUtils.getAccessibleMethod(bean.getClass(), mappedWriteMethod);
    		if (mappedWriteMethod == null) {
    			throw new NoSuchMethodException();
    		}
    		final Object[] params = new Object[2];
    		params[0] = key;
    		params[1] = value;
    		invokeMethod(mappedWriteMethod, bean, params);
    	
    	} else {
    		final Method readMethod = getReadMethod(bean.getClass(), descriptor);
    		if (readMethod == null) {
    			throw new NoSuchMethodException();
    		}
    		final Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
    		if (invokeResult instanceof java.util.Map) {
    			final java.util.Map<String, Object> map = toPropertyMap(invokeResult);
    			map.put(key, value);
    		}
    	}
    }
    
    
    
    public void setSimpleProperty(final Object bean, final String name, final Object value) 
    		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
    	
    	if (bean == null || name == null) {
    		throw new IllegalArgumentException();
    	}
    	final Class<?> beanClass = bean.getClass();

    	if (resolver.hasNested(name)) {
    		throw new IllegalArgumentException();
    	}
    	if (resolver.isIndexed(name)) {
    		throw new IllegalArgumentException();
    	}
    	if (resolver.isMapped(name)) {
    		throw new IllegalArgumentException();
    	}

    	final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
    	if (descriptor == null) {
    		throw new NoSuchMethodException();
    	}

    	final Method writeMethod = getWriteMethod(beanClass, descriptor);
    	if (writeMethod == null) {
    		throw new NoSuchMethodException();
    	}

    	final Object[] values = new Object[1];
    	values[0] = value;
    	invokeMethod(writeMethod, bean, values);
    }
    
    
    
    public Object getProperty(final Object bean, final String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
        return getNestedProperty(bean, name);
    }
    
	
	
    public Method getReadMethod(final Class<?> clazz, final PropertyDescriptor descriptor) {
        return MethodUtils.getAccessibleMethod(clazz, descriptor.getReadMethod());
    }
	
    
    
    public Method getWriteMethod(final Class<?> clazz, final PropertyDescriptor descriptor) 
    		throws IntrospectionException {
        
    	final BeanIntrospectionData data = getIntrospectionData(clazz);
        return MethodUtils.getAccessibleMethod(clazz, data.getWriteMethod(clazz, descriptor));
    }
    
    
    
    public Object getNestedProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" +
                    bean.getClass() + "'");
        }

        // Resolve nested references
        while (resolver.hasNested(name)) {
            final String next = resolver.next(name);
            Object nestedBean = null;
            if (bean instanceof Map) {
                nestedBean = getPropertyOfMapBean((Map<?, ?>) bean, next);
            } else if (resolver.isMapped(next)) {
                nestedBean = getMappedProperty(bean, next);
            } else if (resolver.isIndexed(next)) {
                nestedBean = getIndexedProperty(bean, next);
            } else {
                nestedBean = getSimpleProperty(bean, next);
            }
            if (nestedBean == null) {
                throw new NestedNullException
                        ("Null property value for '" + name +
                        "' on bean class '" + bean.getClass() + "'");
            }
            bean = nestedBean;
            name = resolver.remove(name);
        }

        if (bean instanceof Map) {
            bean = getPropertyOfMapBean((Map<?, ?>) bean, name);
        } else if (resolver.isMapped(name)) {
            bean = getMappedProperty(bean, name);
        } else if (resolver.isIndexed(name)) {
            bean = getIndexedProperty(bean, name);
        } else {
            bean = getSimpleProperty(bean, name);
        }
        return bean;
    }
    
    
    
    private Object getPropertyOfMapBean(final Map<?, ?> bean, String propertyName)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

            if (resolver.isMapped(propertyName)) {
                final String name = resolver.getProperty(propertyName);
                if (name == null || name.isEmpty()) {
                    propertyName = resolver.getKey(propertyName);
                }
            }

            if (resolver.isIndexed(propertyName) ||
                resolver.isMapped(propertyName)) {
                throw new IllegalArgumentException(
                        "Indexed or mapped properties are not supported on"
                        + " objects of type Map: " + propertyName);
            }

            return bean.get(propertyName);
        }
    
    
	
    private BeanIntrospectionData getIntrospectionData(final Class<?> beanClass) 
    		throws IntrospectionException {
        if (beanClass == null) {
            throw new IllegalArgumentException("No bean class specified");
        }
        
        BeanIntrospectionData data = descriptorsCache.get(beanClass);
        if (data == null) {
            data = fetchIntrospectionData(beanClass);
            descriptorsCache.put(beanClass, data);
        }

        return data;
    }
    
    
    
    private Object invokeMethod(final Method method, final Object bean, final Object[] values)
    		throws IllegalAccessException, InvocationTargetException {
    	if (bean == null) {
    		throw new IllegalArgumentException();
    	}

    	try {
    		return method.invoke(bean, values);
    	} catch (final NullPointerException | IllegalArgumentException cause) {
    		final StringBuilder valueString = new StringBuilder();
    		if (values != null) {
    			for (int i = 0; i < values.length; i++) {
    				if (i>0) {
    					valueString.append(", ");
    				}
    				if (values[i] == null) {
    					valueString.append("<null>");
    				} else {
    					valueString.append(values[i].getClass().getName());
    				}
    			}
    		}
    		final StringBuilder expectedString = new StringBuilder();
    		final Class<?>[] parTypes = method.getParameterTypes();
    		if (parTypes != null) {
    			for (int i = 0; i < parTypes.length; i++) {
    				if (i > 0) {
    					expectedString.append(", ");
    				}
    				expectedString.append(parTypes[i].getName());
    			}
    		}
    		final IllegalArgumentException e = new IllegalArgumentException();
    		throw e;
    	}
    }
    
    
    
    public Object getMappedProperty(final Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" +
                    bean.getClass() + "'");
        }

        // Identify the key of the requested individual property
        String key  = null;
        try {
            key = resolver.getKey(name);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException
                    ("Invalid mapped property '" + name +
                    "' on bean class '" + bean.getClass() + "' " + e.getMessage());
        }
        if (key == null) {
            throw new IllegalArgumentException("Invalid mapped property '" +
                    name + "' on bean class '" + bean.getClass() + "'");
        }

        name = resolver.getProperty(name);
        
        return getMappedProperty(bean, name, key);
    }
    
    
    
    public Object getIndexedProperty(final Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" +
                    bean.getClass() + "'");
        }

        // Identify the index of the requested individual property
        int index = -1;
        try {
            index = resolver.getIndex(name);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid indexed property '" +
                    name + "' on bean class '" + bean.getClass() + "' " +
                    e.getMessage());
        }
        if (index < 0) {
            throw new IllegalArgumentException("Invalid indexed property '" +
                    name + "' on bean class '" + bean.getClass() + "'");
        }

        name = resolver.getProperty(name);

        return getIndexedProperty(bean, name, index);
    }
    
    
    
    public Object getMappedProperty(final Object bean, final String name, final String key)
    		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
    	
    	if (bean == null || name == null || key == null) {
    		throw new IllegalArgumentException("No bean specified");
    	}

    	Object result = null;

    	final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
    	if (descriptor == null) {
    		throw new NoSuchMethodException();
    	}

    	if (descriptor instanceof MappedPropertyDescriptor) {
    		Method readMethod = ((MappedPropertyDescriptor) descriptor).getMappedReadMethod();
    		readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
    		if (readMethod == null) {
    			throw new NoSuchMethodException();
    		}
    		final Object[] keyArray = new Object[1];
    		keyArray[0] = key;
    		result = invokeMethod(readMethod, bean, keyArray);
    	} else {
    		final Method readMethod = getReadMethod(bean.getClass(), descriptor);
    		if (readMethod == null) {
    			throw new NoSuchMethodException();
    		}
    		final Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
    		if (invokeResult instanceof java.util.Map) {
    			result = ((java.util.Map<?, ?>)invokeResult).get(key);
    		}
    	}
    	
    	return result;
    }
    
    
    
    public Object getIndexedProperty(final Object bean, final String name, final int index)
    		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NestedNullException, IntrospectionException {
    	
    	if (bean == null) {
    		throw new IllegalArgumentException("No bean specified");
    	}

    	if (name == null || name.isEmpty()) {
    		if (bean.getClass().isArray()) {
    			return Array.get(bean, index);
    		}
    		if (bean instanceof List) {
    			return ((List<?>)bean).get(index);
    		}
    	}

    	if (name == null) {
    		throw new IllegalArgumentException();
    	}

    	final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
    	if (descriptor == null) {
    		throw new NoSuchMethodException();
    	}

    	if (descriptor instanceof IndexedPropertyDescriptor) {
    		Method readMethod = ((IndexedPropertyDescriptor) descriptor).getIndexedReadMethod();
    		readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
    		if (readMethod != null) {
    			final Object[] subscript = new Object[1];
    			subscript[0] = Integer.valueOf(index);
    			try {
    				return invokeMethod(readMethod,bean, subscript);
    			} catch (final InvocationTargetException e) {
    				if (e.getTargetException() instanceof IndexOutOfBoundsException) {
    					throw (IndexOutOfBoundsException)
    					e.getTargetException();
    				}
    				throw e;
    			}
    		}
    	}

    	final Method readMethod = getReadMethod(bean.getClass(), descriptor);
    	if (readMethod == null) {
    		throw new NoSuchMethodException();
    	}

    	final Object value = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
    	if (!value.getClass().isArray()) {
    		if (!(value instanceof java.util.List)) {
    			throw new IllegalArgumentException();
    		}
    		return ((java.util.List<?>) value).get(index);
    	}

    	try {
    		return Array.get(value, index);
    	} catch (final ArrayIndexOutOfBoundsException e) {
    		throw new ArrayIndexOutOfBoundsException();
    	}
    }
    
    
    
    private Map<Class<?>, Map> getMappedPropertyDescriptors(final Class<?> beanClass) {
        if (beanClass == null) {
            return null;
        }
        return mappedDescriptorsCache.get(beanClass);
    }
    
    
    
    private Map getMappedPropertyDescriptors(final Object bean) {
        if (bean == null) {
            return null;
        }
        return getMappedPropertyDescriptors(bean.getClass());
    }
    
    
    
    private static List<Object> toObjectList(final Object obj) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) obj;
        return list;
    }
    
    
    
    private static Map<String, Object> toPropertyMap(final Object obj) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) obj;
        return map;
    }
    
    
    
    private BeanIntrospectionData fetchIntrospectionData(final Class<?> beanClass) 
    		throws IntrospectionException {
        
    	final DefaultIntrospectionContext ictx = new DefaultIntrospectionContext(beanClass);

        for (final BeanIntrospector bi : introspectors) {
            bi.introspect(ictx);
        }

        return new BeanIntrospectionData(ictx.getPropertyDescriptors());
    }
}
