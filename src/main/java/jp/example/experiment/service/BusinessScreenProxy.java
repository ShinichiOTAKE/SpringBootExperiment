package jp.example.experiment.service;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import jp.example.experiment.dto.request.InvokeEventDto;
import jp.example.experiment.dto.request.RequestDto;
import jp.example.experiment.dto.response.impl.Smp00010ResponseDto;

@Service
public final class BusinessScreenProxy implements BusinessScreenService {
	
	private final String SUFFIX_SERVICE_CLASS_NAME = "Service";
	private final String SEPARATOR_METHOD_NAME = "_";
	
	private final BeanFactory beanFactory;

	private BusinessScreenProxy(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public List<Smp00010ResponseDto>  excute(InvokeEventDto event, RequestDto request) throws Throwable {
	
		checkRequest(event, request);
		
		BusinessScreenService service = 
				this.beanFactory.getBean(event.getScreenId() + SUFFIX_SERVICE_CLASS_NAME,
				BusinessScreenService.class);
		
		Class<?> serviceClass = service.getClass();
		Method method = serviceClass.getMethod(event.getElementId() + SEPARATOR_METHOD_NAME + event.getEventName(), RequestDto.class);
		
		return (List<Smp00010ResponseDto>) method.invoke(service, request);
	}
	
	private void checkRequest(InvokeEventDto event, RequestDto reqest) throws Throwable {
		;
	}
}
