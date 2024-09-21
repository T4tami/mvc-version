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
@Table(name = "transplantRecord")
@Data
public class TransplantRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ps_id")
	private ProductSchedule productSchedule;

	private Date actTransDate;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "stock_id")
	private Stock stock;
	@Column(name = "act_board_count")
	private Integer actBoardCount;
	private String stage;
	private String remark;
}
