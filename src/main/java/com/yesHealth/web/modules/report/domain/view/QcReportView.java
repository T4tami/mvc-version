package com.yesHealth.web.modules.report.domain.view;

import java.util.Date;

import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "vw_qc_report")
@Data
public class QcReportView {
	@Id
	private Long id;
	private String manuNo;
	private String productName;
	private Long actBoardCount;
	private Date estHarvestDate;
	private String position;
	private String stage;
	private Date actTransDate;
	private Long idealHeight;
	private String plantLux;
	private String stockLux;
}
