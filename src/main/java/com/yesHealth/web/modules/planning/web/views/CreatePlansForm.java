package com.yesHealth.web.modules.planning.web.views;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreatePlansForm {
	List<CreatePlanForm> CreatePlanFormList = new ArrayList<>();

}
