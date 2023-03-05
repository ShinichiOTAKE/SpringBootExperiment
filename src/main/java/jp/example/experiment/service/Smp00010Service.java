package jp.example.experiment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.example.experiment.dto.request.Smp00010RequestDto;
import jp.example.experiment.dto.response.impl.Smp00010ResponseDto;
import jp.example.experiment.entity.ProductsEntity;
import jp.example.experiment.repository.ProductsRepository;

@Service
public class Smp00010Service {
	
	private final ProductsRepository productsRepository;
	
	public Smp00010Service(ProductsRepository productsRepository) {
		
		this.productsRepository = productsRepository;
		
	}

	public List<Smp00010ResponseDto> getProductsById(Smp00010RequestDto request) {
	
		List<ProductsEntity> entities = productsRepository.getProductsById(request);
		
		var responses = new ArrayList<Smp00010ResponseDto>();
		
		for(ProductsEntity entity: entities) {
			
			var response = new Smp00010ResponseDto();
			
			response.setId(entity.getId());
			response.setSequenceNo(entity.getSequenceNo());
			response.setName(entity.getName());
			
			responses.add(response);
			
		}
		
		return responses;
	}

}
