package jp.example.experiment.util;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultIntrospectionContext implements IntrospectionContext {
	
	static final PropertyDescriptor[] EMPTY_ARRAY = {};
	
	
	
	private final Class<?> currentClass;
	private final Map<String, PropertyDescriptor> descriptors;
	
	

    public DefaultIntrospectionContext(final Class<?> cls) {
        currentClass = cls;
        descriptors = new HashMap<>();
    }
    
    
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        return descriptors.values().toArray(EMPTY_ARRAY);
    }



	@Override
	public PropertyDescriptor getPropertyDescriptor(String name) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}



	@Override
	public Class<?> getTargetClass() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}



	@Override
	public boolean hasProperty(String name) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}



	@Override
	public Set<String> propertyNames() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}



	@Override
	public void removePropertyDescriptor(String name) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	
	
    @Override
    public void addPropertyDescriptor(final PropertyDescriptor desc) {
        if (desc == null) {
            throw new IllegalArgumentException(
                    "Property descriptor must not be null!");
        }
        descriptors.put(desc.getName(), desc);
    }

    

    @Override
    public void addPropertyDescriptors(final PropertyDescriptor[] descs) {
        if (descs == null) {
            throw new IllegalArgumentException(
                    "Array with descriptors must not be null!");
        }

        for (final PropertyDescriptor desc : descs) {
            addPropertyDescriptor(desc);
        }
    }
}
