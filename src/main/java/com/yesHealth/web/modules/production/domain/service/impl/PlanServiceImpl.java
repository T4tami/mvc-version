package com.yesHealth.web.modules.production.domain.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.ValidationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.product.domain.entity.Product;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.production.domain.exception.YhValidateException;
import com.yesHealth.web.modules.production.domain.respository.PlanRepository;
import com.yesHealth.web.modules.production.domain.respository.StockRepository;
import com.yesHealth.web.modules.production.domain.service.OrderService;
import com.yesHealth.web.modules.production.domain.service.PlanService;
import com.yesHealth.web.modules.production.web.views.CreatePlansForm;
import com.yesHealth.web.modules.production.web.views.EditPlanForm;
import com.yesHealth.web.modules.production.web.views.PlanForm;
import com.yesHealth.web.modules.report.domain.entity.SeedReport;
import com.yesHealth.web.modules.report.domain.repository.SeedReportRepository;
import com.yesHealth.web.modules.util.service.DateUtil;
import com.yesHealth.web.modules.util.service.MessageService;
import com.yesHealth.web.modules.util.service.PlanValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlanServiceImpl implements PlanService {
	private PlanRepository planRepository;
	private StockRepository stockRepository;
	private SeedReportRepository seedReportRepository;
	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";

	public PlanServiceImpl(PlanRepository planRepository, StockRepository stockRepository,
			MessageService messageService, SeedReportRepository seedReportRepository) {
		this.planRepository = planRepository;
		this.stockRepository = stockRepository;
		this.seedReportRepository = seedReportRepository;
	}

	@Override
	public Page<ProductSchedule> findAll(Pageable pageable) {
		Page<ProductSchedule> plans = planRepository.findAllByStatus(NOT_IMPLEMENTEDSTATUS, pageable);
		return plans;
	}

	@Override
	public Page<ProductSchedule> findByHarvestDateBetweenAndStatus(String startDateStr, String endDateStr,
			String Status, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return planRepository.findByHarvestDateBetweenAndStatus(formateStartDate, formateEndDate, Status, pageable);
	}

	@Override
	public void saveProductSchedule(CreatePlansForm createPlansForm) throws ParseException, YhValidateException {

		Map<String, Object> businessErrors = PlanValidator.validateCreatePlan(createPlansForm);
		if (!businessErrors.isEmpty()) {
			throw new YhValidateException("Validation failed", businessErrors);
		}

		int index = 0;
		for (PlanForm createPlanForm : createPlansForm.getCreatePlanFormList()) {

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			String manuNo = OrderService.generateOrderNo(++index);
			ProductSchedule productSchedule = ProductSchedule.builder().manuNo(manuNo)
					.product(new Product(createPlanForm.getProductId())).targetWeight(createPlanForm.getTargetWeight())
					.status(NOT_IMPLEMENTEDSTATUS).seedingBoardCount(createPlanForm.getSeedingBoardCount())
					.wateringBoardCount(createPlanForm.getWateringBoardCount())
					.headOutBoardCount(createPlanForm.getHeadOutBoardCount())
					.growingBoardCount(createPlanForm.getGrowingBoardCount())
					.matureBoardCount(createPlanForm.getMatureBoardCount())
					.harvestBoardCount(createPlanForm.getHarvestBoardCount())
					.sStockId(new Stock(createPlanForm.getSStockId())).gStockId(new Stock(createPlanForm.getGStockId()))
					.pStockId(new Stock(createPlanForm.getPStockId()))
					.seedingDate(format.parse(createPlanForm.getSeedingDate()))
					.wateringDate(format.parse(createPlanForm.getWateringDate()))
					.headOutDate(format.parse(createPlanForm.getHeadOutDate()))
					.growingDate(format.parse(createPlanForm.getGrowingDate()))
					.matureDate(createPlanForm.getMatureDate() != null ? format.parse(createPlanForm.getMatureDate())
							: null)
					.harvestDate(format.parse(createPlanForm.getHarvestDate())).createDate(new Date()).build();

			ProductSchedule savePlan = planRepository.save(productSchedule);
			log.info(savePlan.toString());

			SeedReport seedReport = SeedReport.builder().ps(productSchedule).workDate(new Date()).Remark(null).build();
			seedReportRepository.save(seedReport);

		}

	}

	@Override
	public EditPlanForm findbyId(Long planId) {
		return mapToEditForm(planRepository.findById(planId).get());
	}

	private Stock findStockById(List<Stock> stocks, Long stockId) {
		try {
			return stocks.stream().filter(stock -> stock.getId().equals(stockId)).findFirst()
					.orElseThrow(() -> new Exception("Product not found"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Helper methods
//	private Product findProductById(List<Product> products, Long productId) {
//		try {
//			return products.stream().filter(product -> product.getId().equals(productId)).findFirst()
//					.orElseThrow(() -> new Exception("Product not found"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	private EditPlanForm mapToEditForm(ProductSchedule ps) {
		return EditPlanForm.builder().id(ps.getId()).harvestStage(ps.getProduct().getHarvestStage())
				.manuNo(ps.getManuNo()).targetWeight(ps.getTargetWeight()).productId(ps.getProduct().getId())
				.sStockId(ps.getSStockId().getId()).gStockId(ps.getGStockId().getId())
				.pStockId(ps.getPStockId().getId())
				.seedingDate(DateUtil.convertDateToString(ps.getSeedingDate(), "yyyy-MM-dd"))
				.wateringDate(DateUtil.convertDateToString(ps.getWateringDate(), "yyyy-MM-dd"))
				.headOutDate(DateUtil.convertDateToString(ps.getHeadOutDate(), "yyyy-MM-dd"))
				.growingDate(DateUtil.convertDateToString(ps.getGrowingDate(), "yyyy-MM-dd"))
				.matureDate(DateUtil.convertDateToString(ps.getMatureDate(), "yyyy-MM-dd"))
				.harvestDate(DateUtil.convertDateToString(ps.getHarvestDate(), "yyyy-MM-dd"))
				.seedingBoardCount(ps.getSeedingBoardCount()).wateringBoardCount(ps.getWateringBoardCount())
				.headOutBoardCount(ps.getHeadOutBoardCount()).growingBoardCount(ps.getGrowingBoardCount())
				.matureBoardCount(ps.getMatureBoardCount()).harvestBoardCount(ps.getHarvestBoardCount()).build();
	}

	@Override
	public void updateProductSchedule(Long id, EditPlanForm editPlanForm) {

		ProductSchedule ps = mapToProductSchedule(id, editPlanForm);
		planRepository.save(ps);
	}

	private ProductSchedule mapToProductSchedule(Long id, EditPlanForm editPlanForm) {
		Optional<ProductSchedule> ps = planRepository.findById(id);
		if (!ps.isPresent()) {
			throw new ValidationException("no data");
		}
		if (IMPLEMENTEDSTATUS.equals(ps.get().getStatus())) {
			throw new ValidationException("the plan has already implemented!!");
		}

		List<Stock> stocks = stockRepository.findAll();
		Stock sStock = findStockById(stocks, ps.get().getSStockId().getId());
		Stock gStock = findStockById(stocks, ps.get().getGStockId().getId());
		Stock pStock = findStockById(stocks, ps.get().getPStockId().getId());

		return ProductSchedule.builder().id(ps.get().getId()).manuNo(ps.get().getManuNo())
				.product(ps.get().getProduct()).targetWeight(editPlanForm.getTargetWeight())
				.seedingBoardCount(editPlanForm.getSeedingBoardCount())
				.wateringBoardCount(editPlanForm.getWateringBoardCount())
				.headOutBoardCount(editPlanForm.getHeadOutBoardCount())
				.growingBoardCount(editPlanForm.getGrowingBoardCount())
				.matureBoardCount(editPlanForm.getMatureBoardCount())
				.harvestBoardCount(editPlanForm.getHarvestBoardCount()).sStockId(sStock).gStockId(gStock)
				.pStockId(pStock).status(ps.get().getStatus())
				.seedingDate(DateUtil.convertStringToDate(editPlanForm.getSeedingDate(), "yyyy-MM-dd"))
				.wateringDate(DateUtil.convertStringToDate(editPlanForm.getWateringDate(), "yyyy-MM-dd"))
				.headOutDate(DateUtil.convertStringToDate(editPlanForm.getHeadOutDate(), "yyyy-MM-dd"))
				.growingDate(DateUtil.convertStringToDate(editPlanForm.getGrowingDate(), "yyyy-MM-dd"))
				.matureDate(DateUtil.convertStringToDate(editPlanForm.getMatureDate(), "yyyy-MM-dd"))
				.harvestDate(DateUtil.convertStringToDate(editPlanForm.getHarvestDate(), "yyyy-MM-dd")).build();
	}
}
