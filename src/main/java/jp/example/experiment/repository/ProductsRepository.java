package jp.example.experiment.repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.example.experiment.dto.request.Smp00010RequestDto;
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
	
	public List<ProductsEntity> getProductsById(final Smp00010RequestDto dto) {
	
		return (List<ProductsEntity>) jt.query(GET_PRODUCTS_BY_ID_SQL,
				new BeanPropertySqlParameterSource(dto),
				new BeanPropertyRowMapper<ProductsEntity>(ProductsEntity.class));
	}

}
