package jp.example.experiment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jp.example.experiment.dto.request.InvokeEventDto;
import jp.example.experiment.dto.request.impl.Smp00010RequestDto;
import jp.example.experiment.dto.response.impl.Smp00010ResponseDto;
import jp.example.experiment.service.BusinessScreenProxy;

@RestController
public class OverrideMethodController {
	
	private BusinessScreenProxy proxy;
	
	public OverrideMethodController(BusinessScreenProxy proxy) {
		
		this.proxy = proxy;
		
	}

	@PostMapping(
			value="/override/smp00010",
			headers={"X-HTTP-Method-Override=GET"})
	@ResponseBody
	public List<Smp00010ResponseDto> Smp00010Contoroller(@RequestBody final Smp00010RequestDto request) throws Throwable {
		
		var event = new InvokeEventDto();
		
		event.setScreenId("Smp00010");
		event.setElementId("searchButton");
		event.setEventName("onClick");
		
		return (List<Smp00010ResponseDto>) this.proxy.excute(event, request);
		
		//return smp00010Service.getProductsById(request);
		
	}
}
