package com.yesHealth.web.modules.product.domain.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
	public Stock(Long sStockId) {
		this.id = sStockId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String zone;
	private String stage;
	private String building;
	private String floor;
	private String position;
	private String lux;
	private Integer boardCount;

}
