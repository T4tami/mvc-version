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
@Table(name = "productSchedule")
@Data
public class ProductSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String manuNo;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Product product;
	@Column(name = "est_s_board_count")
	private Integer estSBoardCount;
	@Column(name = "est_g_board_count")
	private Integer estGBoardCount;
	@Column(name = "est_p_board_count")
	private Integer estPBoardCount;

	private Date estSeedingDate;
	private Date estWateringDate;
	@Column(name = "est_trans_g_date")
	private Date estTransGDate;
	@Column(name = "est_trans_P_date")
	private Date estTransPDate;
	private Date estHarvestDate;

}
