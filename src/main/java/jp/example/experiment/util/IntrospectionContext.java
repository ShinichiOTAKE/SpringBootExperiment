package jp.example.experiment.util;

import java.beans.PropertyDescriptor;
import java.util.Set;

public interface IntrospectionContext {

	void addPropertyDescriptors(PropertyDescriptor[] descriptors);
	public void addPropertyDescriptor(PropertyDescriptor desc);
	PropertyDescriptor getPropertyDescriptor(String name);
	Class<?> getTargetClass();
	boolean hasProperty(String name);
	Set<String> propertyNames();
	void removePropertyDescriptor(String name);
	
}
