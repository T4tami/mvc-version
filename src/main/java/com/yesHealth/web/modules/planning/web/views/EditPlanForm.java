package com.yesHealth.web.modules.planning.web.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EditPlanForm extends PlanForm {
	private Long id;
	private String harvestStage;
	private String manuNo;
}
