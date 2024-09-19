package com.yesHealth.web.modules.planning.web.views;

import java.time.LocalDate;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreatePlanForm {
	private Integer targetWeight;
	private Long productId;
	private Long sStockId;
	private Long gStockId;
	private Long pStockId;
	private String seedingDate;
	private String wateringDate;
	private String headOutDate;
	private String growingDate;
	private String matureDate;
	private String harvestDate;
	private Integer seedingBoardCount;
	private Integer wateringBoardCount;
	private Integer headOutBoardCount;
	private Integer growingBoardCount;
	private Integer matureBoardCount;
	private Integer harvestBoardCount;
}
