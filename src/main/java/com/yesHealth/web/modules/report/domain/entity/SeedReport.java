package com.yesHealth.web.modules.report.domain.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.util.entity.FileUploadRecords;

import lombok.Data;

@Entity
@Table(name = "seedReport")
@Data
public class SeedReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String seqNo;
	@ManyToOne
	@JoinColumn(name = "ps_id")
	private ProductSchedule ps;
	private Long boardCount;
	private Long boardPiece;
	private Date workDate;
	private Long workMan;
	private String workTimeStart;
	private String workTimeEnd;
	private Double gramBeforeUse;
	private Double gramAfterUse;
	private Integer countPerHole;
	private Double estWorkTime;
	private String Remark;
	private String srcType;
	@ManyToOne
	@JoinColumn(name = "fur_id")
	private FileUploadRecords fileUploadRecords;
}
