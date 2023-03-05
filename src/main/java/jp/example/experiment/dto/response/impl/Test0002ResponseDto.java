package jp.example.experiment.dto.response.impl;

import java.util.Map;

import jp.example.experiment.dto.response.TransformableJson;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Test0002ResponseDto implements TransformableJson {

	private Map<String, String> map;

}
