package com.yesHealth.web.modules.product.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "ProductSchedule")
@Data
public class ProductSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String manuNo;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Product product;
	private Integer targetWeight;
	private Integer seedingBoardCount;
	private Integer wateringBoardCount;
	private Integer headOutBoardCount;
	private Integer growingBoardCount;
	private Integer matureBoardCount;
	private Integer harvestBoardCount;
	@ManyToOne
	@JoinColumn(name = "s_stock_id")
	private Stock sStockId;
	@ManyToOne
	@JoinColumn(name = "g_stock_id")
	private Stock gStockId;
	@ManyToOne
	@JoinColumn(name = "p_stock_id")
	private Stock pStockId;

	private Date seedingDate;
	private Date wateringDate;
	private Date headOutDate;
	private Date growingDate;
	private Date matureDate;
	private Date harvestDate;

}
