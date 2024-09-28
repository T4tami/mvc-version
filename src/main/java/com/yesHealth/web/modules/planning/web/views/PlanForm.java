package com.yesHealth.web.modules.planning.web.views;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString
@SuperBuilder
@NoArgsConstructor
public class PlanForm {
	@NotNull(message = "業務重量不可為空")
	private Integer targetWeight;
	@NotNull(message = "規格不可為空")
	private Long productId;
	@NotNull(message = "見苗儲位不可為空")
	private Long sStockId;
	@NotNull(message = "育苗儲位不可為空")
	private Long gStockId;
	private Long pStockId;
	@NotNull(message = "播種日期不可為空")
	private String seedingDate;
	@NotNull(message = "壓水日期不可為空")
	private String wateringDate;
	@NotNull(message = "見苗日期不可為空")
	private String headOutDate;
	@NotNull(message = "育苗日期不可為空")
	private String growingDate;
	private String matureDate;
	@NotNull(message = "採收日期不可為空")
	private String harvestDate;
	@NotNull(message = "播種盤數不可為空")
	private Integer seedingBoardCount;
	@NotNull(message = "壓水盤數不可為空")
	private Integer wateringBoardCount;
	@NotNull(message = "見苗盤數不可為空")
	private Integer headOutBoardCount;
	@NotNull(message = "育苗盤數不可為空")
	private Integer growingBoardCount;
	private Integer matureBoardCount;
	@NotNull(message = "採收盤數不可為空")
	private Integer harvestBoardCount;
}
