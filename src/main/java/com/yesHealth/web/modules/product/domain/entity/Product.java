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
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	public Product(Long productId) {
		this.id = productId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String productNo;
	private String productName;
	private String specs;
	private String unit;
	private String family;
	private Integer dDays;
	private Integer rDays;
	private Integer sDays;
	private Integer gDays;
	private Integer pDays;

	private Integer sHole;
	private Integer gHole;
	private Integer pHole;

	private Double dRate;
	private Double rRate;
	private Double sRate;
	private Double gRate;
	private Double pRate;

	private String sLux;
	private String gLux;
	private String pLux;
	private Integer estWeight;
	private String type;
	private Integer status;
	private String harvestStage;

}
