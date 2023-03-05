package jp.example.experiment.util2;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

public class BeanUtils {
	
	private static final Map<Class<?>, Object> cachedClass = new ConcurrentHashMap<Class<?>, Object>();

	public static void copyProperties(Object source, Object destination) {
		
		if (source == null || destination == null) {
			return;
		}
		
		PropertyDescriptor[] destinationPds = getPropertyDescriptors(destination.getClass());
		
		for (PropertyDescriptor destPd: destinationPds) {
			Method writeMethod = destPd.getWriteMethod();
			if (writeMethod != null) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), writeMethod.getName());
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null) {
						ResolvableType sourceRt = ResolvableType.forMethodReturnType(readMethod);
						ResolvableType destinationRt = ResolvableType.forMethodParameter(writeMethod, 0);
						
						boolean isAssignable =
							((sourceRt.hasUnresolvableGenerics() || destinationRt.hasUnresolvableGenerics()) ?
								ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType()) :
								destinationRt.isAssignableFrom(sourceRt));
						if (isAssignable) {
							if (Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()) == false) {
								writeMethod.setAccessible(true);
							}
							if (Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) == false) {
								readMethod.setAccessible(true);
							}
							
							try {
								Object value = readMethod.invoke(source);
								writeMethod.invoke(destination, value);
							}
							catch(IllegalAccessException | InvocationTargetException e) {
								continue;
							}

						}
					}
				}
			}
		}
		
	}
	
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
		
		return null;
		
	}
	
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName){
		
		return null;
	
	}
}