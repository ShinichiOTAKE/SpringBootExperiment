package jp.example.experiment.dto.response.impl;

import jp.example.experiment.dto.response.TransformableJson;
import lombok.Data;

@Data
public class Smp00010ResponseDto implements TransformableJson {

	private int id;
	
	private int sequenceNo;
	
	private String name;

}
