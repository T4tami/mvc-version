package com.yesHealth.web.modules.planning.web.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreatePlansForm {
	@Valid
	private List<PlanForm> createPlanFormList = new ArrayList<>(Collections.singletonList(new PlanForm()));

}
