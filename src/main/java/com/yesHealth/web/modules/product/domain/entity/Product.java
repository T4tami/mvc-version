package com.yesHealth.web.modules.product.domain.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "product")
@Data
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String productNo;
	private String productName;
	private String specs;
	private String unit;
	private String family;
	private Integer sDays;
	private Integer gDays;
	private Integer pDays;

	private Integer sHole;
	private Integer gHole;
	private Integer pHole;
	
	private Integer status;
	private String type;
	private String sLux;
	private String gLux;
	private String pLux;
	
	
}
