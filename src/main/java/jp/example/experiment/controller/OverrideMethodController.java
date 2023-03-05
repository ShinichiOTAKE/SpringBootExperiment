package jp.example.experiment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jp.example.experiment.dto.request.Smp00010RequestDto;
import jp.example.experiment.dto.response.impl.Smp00010ResponseDto;
import jp.example.experiment.service.Smp00010Service;

@RestController
public class OverrideMethodController {
	
	private Smp00010Service smp00010Service;
	
	public OverrideMethodController(Smp00010Service smp00010Service) {
		
		this.smp00010Service = smp00010Service;
		
	}

	@PostMapping(
			value="/override/smp00010",
			headers={"X-HTTP-Method-Override=GET"})
	@ResponseBody
	public List<Smp00010ResponseDto> Smp00010Contoroller(@RequestBody final Smp00010RequestDto request) {
		
		return smp00010Service.getProductsById(request);
		
	}
}
