package jp.example.experiment.repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.example.experiment.dto.request.RequestDto;
import jp.example.experiment.entity.ProductsEntity;

@Repository
public class ProductsRepository {

	private final NamedParameterJdbcTemplate jt;
	
	public ProductsRepository(NamedParameterJdbcTemplate jt) {
		this.jt = jt;
	}
	
	private final String GET_PRODUCTS_BY_ID_SQL = 
			"SELECT *" +
			"  FROM PRODUCTS" +
			" WHERE ID = :id";
	
	public List<ProductsEntity> getProductsById(final RequestDto request) {
	
		return (List<ProductsEntity>) jt.query(GET_PRODUCTS_BY_ID_SQL,
				new BeanPropertySqlParameterSource(request),
				new BeanPropertyRowMapper<ProductsEntity>(ProductsEntity.class));
	}

}
