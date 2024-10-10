package com.yesHealth.web.modules.report.domain.entity;

import java.time.LocalDateTime;
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
@Table(name = "waterReport")
@Data
public class WaterReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String seqNo;
	@ManyToOne
	@JoinColumn(name = "ps_id")
	private ProductSchedule ps;
	private Long boardCount;
	private Date workDate;
	private Long workMan;
	private LocalDateTime workTimeStart;
	private LocalDateTime workTimeEnd;
	private String darkRoomPosition;
	private String Remark;
	private String srcType;
	@ManyToOne
	@JoinColumn(name = "fur_id")
	private FileUploadRecords fileUploadRecords;
}
