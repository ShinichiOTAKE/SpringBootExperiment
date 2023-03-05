package jp.example.experiment.dto.response.impl;

import java.util.List;
import java.util.Map;

import jp.example.experiment.dto.response.TransformableJson;
import lombok.Data;

@Data
public class Test0001ResponseDto implements TransformableJson {
    
	private int key;
	private String value;
	private List<TestChildDto> children;
	private List<String> emptyList;
	private Map<String, String> maps;
	private String filler;
}
