package com.yesHealth.web.modules.planning.domain.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.ValidationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.planning.domain.respository.PlanRepository;
import com.yesHealth.web.modules.planning.domain.respository.StockRepository;
import com.yesHealth.web.modules.planning.domain.respository.TransplantRecordRepository;
import com.yesHealth.web.modules.planning.domain.service.OrderService;
import com.yesHealth.web.modules.planning.domain.service.PlanService;
import com.yesHealth.web.modules.planning.web.views.PlanForm;
import com.yesHealth.web.modules.planning.web.views.CreatePlansForm;
import com.yesHealth.web.modules.planning.web.views.EditPlanForm;
import com.yesHealth.web.modules.product.domain.entity.Product;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.product.domain.repository.ProductRepository;
import com.yesHealth.web.modules.util.MessageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlanServiceImpl implements PlanService {
	private PlanRepository planRepository;
	private StockRepository stockRepository;
	private ProductRepository productRepository;
	private TransplantRecordRepository transplantRecordRepository;
	private MessageService messageService;
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";

	private static final int MAX_BOARD_COUNT = 52;

	public PlanServiceImpl(PlanRepository planRepository, StockRepository stockRepository,
			ProductRepository productRepository, TransplantRecordRepository transplantRecordRepository,
			MessageService messageService) {
		this.planRepository = planRepository;
		this.stockRepository = stockRepository;
		this.productRepository = productRepository;
		this.transplantRecordRepository = transplantRecordRepository;
		this.messageService = messageService;
	}

	@Override
	public Page<ProductSchedule> findAll(Pageable pageable) {
		Page<ProductSchedule> plans = planRepository.findAllByStatus(NOT_IMPLEMENTEDSTATUS, pageable);
		return plans;
	}

	@Override
	public Page<ProductSchedule> findByestHarvestDateBetween(String startDateStr, String endDateStr,
			Pageable pageable) {
		Date formateStartDate = startDateStr == null ? getStartOfNextWeek() : convertStringToDate(startDateStr);
		Date formateEndDate = endDateStr == null ? getEndOfNextWeek() : convertStringToDate(endDateStr);
		return planRepository.findByHarvestDateBetweenAndStatus(formateStartDate, formateEndDate, NOT_IMPLEMENTEDSTATUS,
				pageable);
	}

	@Override
	public Map<String, Object> validateCreatePlan(CreatePlansForm createPlansForm) {
		List<PlanForm> createPlanList = createPlansForm.getCreatePlanFormList();

		List<Product> products = productRepository.findAll();
		List<String> globalErrorList = new ArrayList<>();
		Map<String, Object> businessErrors = new HashMap<>();

		int index = 0;
		for (PlanForm createPlanForm : createPlanList) {
			index++;
			Long productId = createPlanForm.getProductId();

			Product product = findProductById(products, productId);

			// Lux驗證
			validateLuxErrors(index, product, createPlanForm, globalErrorList);

			// 天數驗證
			validateDaysErrors(index, createPlanForm, product, globalErrorList);

			// 水道盤數驗證
			try {
				validateBoardCountErrors(index, createPlanList, createPlanForm, globalErrorList);
			} catch (ParseException e) {
				log.error("日期格式錯誤，string轉換date失敗", e);
			}
		}

		businessErrors.put("globalError", globalErrorList);
		return businessErrors;
	}

	@Override
	public void saveProductSchedule(CreatePlansForm createPlansForm) {
		List<Stock> stocks = stockRepository.findAll();
		List<Product> products = productRepository.findAll();
		int index = 0;
		for (PlanForm createPlanForm : createPlansForm.getCreatePlanFormList()) {
			Long productId = createPlanForm.getProductId();
			Long sStockId = createPlanForm.getSStockId();
			Long gStockId = createPlanForm.getGStockId();
			Stock pStock = null;
			Date matureDate = null;
			Integer matureBoardCount = createPlanForm.getMatureBoardCount();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				if ("G".equals(findProductById(products, productId).getHarvestStage())) {
					matureBoardCount = null;
					pStock = null;
					matureDate = null;
				} else {
					matureBoardCount = createPlanForm.getMatureBoardCount();
					pStock = findStockById(stocks, createPlanForm.getPStockId());
					matureDate = format.parse(createPlanForm.getMatureDate());
				}

				String manuNo = OrderService.generateOrderNo(++index);
				ProductSchedule productSchedule = ProductSchedule.builder().manuNo(manuNo)
						.product(findProductById(products, productId)).targetWeight(createPlanForm.getTargetWeight())
						.status(NOT_IMPLEMENTEDSTATUS).seedingBoardCount(createPlanForm.getSeedingBoardCount())
						.wateringBoardCount(createPlanForm.getWateringBoardCount())
						.headOutBoardCount(createPlanForm.getHeadOutBoardCount())
						.growingBoardCount(createPlanForm.getGrowingBoardCount()).matureBoardCount(matureBoardCount)
						.harvestBoardCount(createPlanForm.getHarvestBoardCount())
						.sStockId(findStockById(stocks, sStockId)).gStockId(findStockById(stocks, gStockId))
						.pStockId(pStock).seedingDate(format.parse(createPlanForm.getSeedingDate()))
						.wateringDate(format.parse(createPlanForm.getWateringDate()))
						.headOutDate(format.parse(createPlanForm.getHeadOutDate()))
						.growingDate(format.parse(createPlanForm.getGrowingDate())).matureDate(matureDate)
						.harvestDate(format.parse(createPlanForm.getHarvestDate())).createDate(new Date()).build();
				ProductSchedule savePlan = planRepository.save(productSchedule);
				log.info(savePlan.toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public EditPlanForm findbyId(Long planId) {
		return mapToEditForm(planRepository.findById(planId).get());
	}

	private void validateDaysErrors(int index, PlanForm createPlanForm, Product product, List<String> globalErrorList) {
		long actualSDays = daysDifference(createPlanForm.getGrowingDate(), createPlanForm.getHeadOutDate());
		String harvestStage = product.getHarvestStage();
		String specs = product.getSpecs();
		if (!validDays(product.getSDays(), actualSDays)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.sDays.error",
					new Object[] { specs, product.getSDays() }));
		}

		if (!"G".equals(harvestStage)) {
			long actualGDays = daysDifference(createPlanForm.getMatureDate(), createPlanForm.getGrowingDate());
			if (!validDays(product.getGDays(), actualGDays)) {
				globalErrorList.add(messageService.getMessage("plan.createForm.gDays.error",
						new Object[] { specs, product.getGDays() }));
			}

			long actualPDays = daysDifference(createPlanForm.getHarvestDate(), createPlanForm.getMatureDate());
			if (!validDays(product.getPDays(), actualPDays)) {
				globalErrorList.add(
						messageService.getMessage("plan.createForm.pDays.error", new Object[] { product.getPDays() }));
			}
		} else {
			long actualGDays = daysDifference(createPlanForm.getHarvestDate(), createPlanForm.getGrowingDate());
			if (!validDays(product.getGDays(), actualGDays)) {
				globalErrorList.add(messageService.getMessage("plan.createForm.gType.gDays.error",
						new Object[] { specs, product.getGDays() }));
			}
		}
	}

	private void validateBoardCountErrors(int index, List<PlanForm> createPlanList, PlanForm createPlanForm,
			List<String> globalErrorList) throws ParseException {
		List<Product> products = productRepository.findAll();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date growingDate = format.parse(createPlanForm.getGrowingDate());
		List<Stock> stocks = stockRepository.findAll();
		Long gStockId = createPlanForm.getGStockId();
		Long pStockId = createPlanForm.getPStockId();
		Long productId = createPlanForm.getProductId();

		Product product = findProductById(products, productId);
		String harvestStage = product.getHarvestStage();
		String specs = product.getSpecs();
		if (!"G".equals(harvestStage)) {
			validateGBoardCount(growingDate, createPlanList, stocks, gStockId, specs, globalErrorList);
			validatePBoardCount(format.parse(createPlanForm.getMatureDate()), createPlanList, stocks, pStockId, specs,
					globalErrorList);
		} else {
			validateGBoardCount(growingDate, createPlanList, stocks, gStockId, specs, globalErrorList);
		}
	}

	private void validateGBoardCount(Date date, List<PlanForm> createPlanList, List<Stock> stocks, Long gStockId,
			String specs, List<String> globalErrorList) {
		Long formTotalGBoardCount = calculateTotalGBoardCount(createPlanList, gStockId);
		Long planTotalGBoardCount = planRepository.sumGBoardCount(gStockId, getDateWithoutTime(date));
		Long actTotalGBoardCount = transplantRecordRepository.sumActGBoardCountByStage(gStockId,
				getDateWithoutTime(date), "G");

		if (Long.sum(formTotalGBoardCount, planTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.gPosition.limit1",
					new Object[] { specs, findStockById(stocks, gStockId).getPosition(), MAX_BOARD_COUNT }));
		}
		if (Long.sum(formTotalGBoardCount, actTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.gPosition.limit2",
					new Object[] { specs, findStockById(stocks, gStockId).getPosition(), MAX_BOARD_COUNT }));
		}
	}

	private void validatePBoardCount(Date date, List<PlanForm> createPlanList, List<Stock> stocks, Long pStockId,
			String specs, List<String> globalErrorList) {
		Long formTotalPBoardCount = calculateTotalPBoardCount(createPlanList, pStockId);
		Long planTotalPBoardCount = planRepository.sumPBoardCount(pStockId, getDateWithoutTime(date));
		Long actTotalPBoardCount = transplantRecordRepository.sumActPBoardCountByStage(pStockId,
				getDateWithoutTime(date), "G");

		if (Long.sum(formTotalPBoardCount, planTotalPBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.pPosition.limit1",
					new Object[] { specs, findStockById(stocks, pStockId).getPosition(), MAX_BOARD_COUNT }));
		}
		if (Long.sum(formTotalPBoardCount, actTotalPBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.pPosition.limit2",
					new Object[] { specs, findStockById(stocks, pStockId).getPosition(), MAX_BOARD_COUNT }));
		}
	}

	private Long calculateTotalGBoardCount(List<PlanForm> createPlanList, Long gStockId) {
		Long totalGBoardCount = 0L;
		for (PlanForm form : createPlanList) {
			if (form.getGStockId().equals(gStockId)) {
				totalGBoardCount = Long.sum(form.getGrowingBoardCount(), totalGBoardCount);
			}
		}
		return totalGBoardCount;
	}

	private Long calculateTotalPBoardCount(List<PlanForm> createPlanList, Long pStockId) {
		Long totalPBoardCount = 0L;
		for (PlanForm form : createPlanList) {
			if (form.getPStockId().equals(pStockId)) {
				totalPBoardCount = Long.sum(form.getMatureBoardCount(), totalPBoardCount);
			}
		}
		return totalPBoardCount;
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
	private Product findProductById(List<Product> products, Long productId) {
		try {
			return products.stream().filter(product -> product.getId().equals(productId)).findFirst()
					.orElseThrow(() -> new Exception("Product not found"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private int findLuxByStockId(List<Stock> stocks, Long stockId) {
		return stocks.stream().filter(stock -> stock.getId().equals(stockId))
				.mapToInt(stock -> Integer.parseInt(stock.getLux())).findFirst().orElse(0); // 或根據需求返回其他值
	}

	private boolean validateLux(int expectedLux, int actualLux) {
		boolean valid = false;
		if (expectedLux != actualLux) {
			return valid;
		}
		return !valid;
	}

	private long daysDifference(String subtractingDate, String subtractedDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate subtracted = LocalDate.parse(subtractedDate, formatter);
		LocalDate subtracting = LocalDate.parse(subtractingDate, formatter);
		return ChronoUnit.DAYS.between(subtracted, subtracting);
	}

	private boolean validDays(Integer expectedDays, long actualDays) {
		boolean valid = false;
		if (Long.compare(actualDays, expectedDays) < 0) {
			return valid;
		}
		return !valid;
	}

	// 获取下周的第一天（周一）
	private Date getStartOfNextWeek() {
		LocalDate today = LocalDate.now();
		LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		return convertToDate(nextMonday);
	}

	// 获取下周的最后一天（周五）
	private Date getEndOfNextWeek() {
		LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate nextFriday = nextMonday.plusDays(4);
		return convertToDate(nextFriday);
	}

	// 将 LocalDate 转换为 Date
	private Date convertToDate(LocalDate localDate) {
		LocalDateTime localDateTime = localDate.atStartOfDay(); // 默认时间是 00:00:00
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private Date convertStringToDate(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null; // 或抛出自定义异常
		}
	}

	private String convertDateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		return formatter.format(date);
	}

	private Date getDateWithoutTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private void validateLuxErrors(int index, Product product, PlanForm createPlanForm, List<String> globalErrorList) {
		List<Stock> stocks = stockRepository.findAll();
		Long sStockId = createPlanForm.getSStockId();
		Long gStockId = createPlanForm.getGStockId();
		Long pStockId = createPlanForm.getPStockId();
		String harvestStage = product.getHarvestStage();
		String specs = product.getSpecs();
		int productSLux = Integer.parseInt(product.getSLux());
		int productGLux = Integer.parseInt(product.getGLux());
		int productPLux = !"G".equals(harvestStage) ? Integer.parseInt(product.getPLux()) : 0;

		int stockSLux = findLuxByStockId(stocks, sStockId);
		int stockGLux = findLuxByStockId(stocks, gStockId);
		int stockPLux = !"G".equals(harvestStage) ? findLuxByStockId(stocks, pStockId) : 0;

		if (!validateLux(productSLux, stockSLux)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.stockSLux.error",
					new Object[] { index, specs, productSLux }));
		}
		if (!validateLux(productGLux, stockGLux)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.stockGLux.error",
					new Object[] { index, specs, productGLux }));
		}
		if (!"G".equals(harvestStage)) {
			if (productPLux == 340 && !validateLux(productPLux, stockPLux)) {
				globalErrorList.add(messageService.getMessage("plan.createForm.stockPLux.error",
						new Object[] { index, specs, productPLux }));
			} else if (productPLux == 440 && stockPLux < productPLux) {
				globalErrorList.add(messageService.getMessage("plan.createForm.stockPLux.range.error",
						new Object[] { index, specs, productPLux }));
			}
		}
	}

	private EditPlanForm mapToEditForm(ProductSchedule ps) {
		return EditPlanForm.builder().id(ps.getId()).harvestStage(ps.getProduct().getHarvestStage())
				.manuNo(ps.getManuNo()).targetWeight(ps.getTargetWeight()).productId(ps.getProduct().getId())
				.sStockId(ps.getSStockId().getId()).gStockId(ps.getGStockId().getId())
				.pStockId(ps.getPStockId().getId()).seedingDate(convertDateToString(ps.getSeedingDate()))
				.wateringDate(convertDateToString(ps.getWateringDate()))
				.headOutDate(convertDateToString(ps.getHeadOutDate()))
				.growingDate(convertDateToString(ps.getGrowingDate()))
				.matureDate(convertDateToString(ps.getMatureDate()))
				.harvestDate(convertDateToString(ps.getHarvestDate())).seedingBoardCount(ps.getSeedingBoardCount())
				.wateringBoardCount(ps.getWateringBoardCount()).headOutBoardCount(ps.getHeadOutBoardCount())
				.growingBoardCount(ps.getGrowingBoardCount()).matureBoardCount(ps.getMatureBoardCount())
				.harvestBoardCount(ps.getHarvestBoardCount()).build();
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
				.seedingDate(convertStringToDate(editPlanForm.getSeedingDate()))
				.wateringDate(convertStringToDate(editPlanForm.getWateringDate()))
				.headOutDate(convertStringToDate(editPlanForm.getHeadOutDate()))
				.growingDate(convertStringToDate(editPlanForm.getGrowingDate()))
				.matureDate(convertStringToDate(editPlanForm.getMatureDate()))
				.harvestDate(convertStringToDate(editPlanForm.getHarvestDate())).build();
	}
}
