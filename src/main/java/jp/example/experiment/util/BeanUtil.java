package jp.example.experiment.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class BeanUtil {
	
	private final PropertyUtilsBean propertyUtilsBean;
	
	public BeanUtil() {
		this.propertyUtilsBean = new PropertyUtilsBean();
	}

	public void copyProperties(final Object source, final Object destination)
			throws IllegalAccessException, InvocationTargetException, NestedNullException, IntrospectionException, NoSuchMethodException {
	
		if (source == null || destination == null) {
			throw new IllegalArgumentException();
		}
		
		final PropertyDescriptor[] sourceDescriptors = getPropertyUtils().getPropertyDescriptors(source);
		
		for (final PropertyDescriptor sourceDescriptor: sourceDescriptors) {
			
			final String name = sourceDescriptor.getName();
			
			if (name.equals("class")) {
				continue;
			}
			
			if (getPropertyUtils().isReadable(source, name) &&
				getPropertyUtils().isWriteable(destination, name)) {
				final Object value = getPropertyUtils().getSimpleProperty(source, name);
				copyProperty(destination, name, value);
			}
		}
	}
	
	
	
	public void copyProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException, NestedNullException, IntrospectionException, NoSuchMethodException {
		
		Object target = bean;
		
		final Resolver resolver = getPropertyUtils().getResolver();
		while (resolver.hasNested(name)) {
			try {
				target = getPropertyUtils().getProperty(target, resolver.next(name));
				name = resolver.remove(name);
			}
			catch (final NoSuchMethodException e) {
				return;
			}
		}
		
		final String propertyName = resolver.getProperty(name);
		Class<?> type = null;
		final int index = resolver.getIndex(name);
		final String key = resolver.getKey(name);
		
		PropertyDescriptor descriptor = null;
		descriptor = getPropertyUtils().getPropertyDescriptor(target, name);
		if (descriptor == null) {
			return;
		}
		
		type = descriptor.getPropertyType();
		if (type == null) {
			return;
		}
		
		if (index >= 0) {
			getPropertyUtils().setIndexedProperty(target, propertyName, index, value);
		}
		else if (key != null) {
			getPropertyUtils().setMappedProperty(target, propertyName, key, value);
		}
		else {
			getPropertyUtils().setSimpleProperty(target, propertyName, value);
		}
	}
	
	
	
	public PropertyUtilsBean getPropertyUtils() {
		
		return propertyUtilsBean;
		
	}
}
