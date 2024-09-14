package com.yesHealth.web.modules.development.domain.entity;

import javax.persistence.*;

import com.yesHealth.web.modules.product.domain.entity.Product;

import lombok.Data;

@Entity
@Data
public class PlantGrowth {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long dayNumber;
	private String stage;
	private Double height;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Product product;
}
