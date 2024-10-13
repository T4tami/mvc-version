package com.yesHealth.web.modules.util.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.product.domain.entity.Product;
import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.product.domain.repository.ProductRepository;
import com.yesHealth.web.modules.production.domain.respository.PlanRepository;
import com.yesHealth.web.modules.production.domain.respository.StockRepository;
import com.yesHealth.web.modules.production.web.views.CreatePlansForm;
import com.yesHealth.web.modules.production.web.views.PlanForm;
import com.yesHealth.web.modules.util.model.ValidBoardCountContent;
import com.yesHealth.web.modules.util.model.ValidDaysContent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlanValidator {
	private static StockRepository stockRepository;
	private static MessageService messageService;
	private static PlanRepository planRepository;
	private static ProductRepository productRepository;
	private static final int MAX_BOARD_COUNT = 52;

	public PlanValidator(StockRepository stockRepository, MessageService messageService, PlanRepository planRepository,
			ProductRepository productRepository) {
		PlanValidator.stockRepository = stockRepository;
		PlanValidator.messageService = messageService;
		PlanValidator.planRepository = planRepository;
		PlanValidator.productRepository = productRepository;
	}

	public static Map<String, Object> validateCreatePlan(CreatePlansForm createPlansForm) {
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
			ValidLuxContent validLuxContent = new ValidLuxContent();
			validLuxContent.setIndex(index);
			validLuxContent.setGlobalErrorList(globalErrorList);
			validLuxContent.setProduct(product);
			validLuxContent.setSStockId(createPlanForm.getSStockId());
			validLuxContent.setGStockId(createPlanForm.getGStockId());
			validLuxContent.setPStockId(createPlanForm.getPStockId());
			PlanValidator.validateLuxErrors(validLuxContent);

			// 天數驗證
			ValidDaysContent validDaysContent = new ValidDaysContent();
			validDaysContent.setIndex(index);
			validDaysContent.setProduct(product);
			validDaysContent.setGlobalErrorList(globalErrorList);
			validDaysContent.setHeadOutDate(createPlanForm.getHeadOutDate());
			validDaysContent.setGrowingDate(createPlanForm.getGrowingDate());
			validDaysContent.setMatureDate(createPlanForm.getMatureDate());
			validDaysContent.setHarvestDate(createPlanForm.getHarvestDate());
			PlanValidator.validateDaysErrors(validDaysContent);

			// 水道盤數驗證
			try {
				ValidBoardCountContent validBoardCountContent = new ValidBoardCountContent();
				validBoardCountContent.setIndex(index);
				validBoardCountContent.setProduct(product);
				validBoardCountContent.setGlobalErrorList(globalErrorList);
				validBoardCountContent.setGStockId(productId);
				validBoardCountContent.setPStockId(productId);
				validBoardCountContent.setGrowingDate(createPlanForm.getGrowingDate());
				validBoardCountContent.setMatureDate(createPlanForm.getMatureDate());
				PlanValidator.validateBoardCountErrors(validBoardCountContent, createPlanList);
			} catch (ParseException e) {
				log.error("日期格式錯誤，string轉換date失敗", e);
			}
		}

		businessErrors.put("globalError", globalErrorList);
		return businessErrors;
	}

	public static void validateLuxErrors(ValidLuxContent validLuxContent) {
		List<Stock> stocks = stockRepository.findAll();

		Long sStockId = validLuxContent.getSStockId();
		Long gStockId = validLuxContent.getGStockId();
		Long pStockId = validLuxContent.getPStockId();
		Product product = validLuxContent.getProduct();
		String harvestStage = product.getHarvestStage();
		String specs = product.getSpecs();

		List<String> globalErrorList = validLuxContent.getGlobalErrorList();
		int index = validLuxContent.getIndex();

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

	public static void validateDaysErrors(ValidDaysContent validDaysContent) {

		List<String> globalErrorList = validDaysContent.getGlobalErrorList();

		String growingDate = validDaysContent.getGrowingDate();
		String headOutDate = validDaysContent.getHeadOutDate();
		long actualSDays = daysDifference(growingDate, headOutDate);

		Product product = validDaysContent.getProduct();
		String harvestStage = product.getHarvestStage();
		String specs = product.getSpecs();
		if (!validDays(product.getSDays(), actualSDays)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.sDays.error",
					new Object[] { specs, product.getSDays() }));
		}

		String matureDate = validDaysContent.getMatureDate();
		String harvestDate = validDaysContent.getHarvestDate();
		if (!"G".equals(harvestStage)) {
			long actualGDays = daysDifference(matureDate, growingDate);
			if (!validDays(product.getGDays(), actualGDays)) {
				globalErrorList.add(messageService.getMessage("plan.createForm.gDays.error",
						new Object[] { specs, product.getGDays() }));
			}

			long actualPDays = daysDifference(harvestDate, matureDate);
			if (!validDays(product.getPDays(), actualPDays)) {
				globalErrorList.add(
						messageService.getMessage("plan.createForm.pDays.error", new Object[] { product.getPDays() }));
			}
		} else {
			long actualGDays = daysDifference(harvestDate, growingDate);
			if (!validDays(product.getGDays(), actualGDays)) {
				globalErrorList.add(messageService.getMessage("plan.createForm.gType.gDays.error",
						new Object[] { specs, product.getGDays() }));
			}
		}
	}

	public static void validateBoardCountErrors(ValidBoardCountContent validBoardCountContent,
			List<PlanForm> createPlanList) throws ParseException {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date growingDate = format.parse(validBoardCountContent.getGrowingDate());
		List<Stock> stocks = validBoardCountContent.getStocks();
		Long gStockId = validBoardCountContent.getGStockId();
		Long pStockId = validBoardCountContent.getPStockId();
		List<String> globalErrorList = validBoardCountContent.getGlobalErrorList();
		Product product = validBoardCountContent.getProduct();
		String harvestStage = product.getHarvestStage();
		String specs = product.getSpecs();
		if (!"G".equals(harvestStage)) {
			validateGBoardCount(growingDate, createPlanList, stocks, gStockId, specs, globalErrorList);
			validatePBoardCount(format.parse(validBoardCountContent.getMatureDate()), createPlanList, stocks, pStockId,
					specs, globalErrorList);
		} else {
			validateGBoardCount(growingDate, createPlanList, stocks, gStockId, specs, globalErrorList);
		}
	}

	private static int findLuxByStockId(List<Stock> stocks, Long stockId) {
		return stocks.stream().filter(stock -> stock.getId().equals(stockId))
				.mapToInt(stock -> Integer.parseInt(stock.getLux())).findFirst().orElse(0); // 或根據需求返回其他值
	}

	private static boolean validateLux(int expectedLux, int actualLux) {
		boolean valid = false;
		if (expectedLux != actualLux) {
			return valid;
		}
		return !valid;
	}

	private static long daysDifference(String subtractingDate, String subtractedDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate subtracted = LocalDate.parse(subtractedDate, formatter);
		LocalDate subtracting = LocalDate.parse(subtractingDate, formatter);
		return ChronoUnit.DAYS.between(subtracted, subtracting);
	}

	private static boolean validDays(Integer expectedDays, long actualDays) {
		boolean valid = false;
		if (Long.compare(actualDays, expectedDays) < 0) {
			return valid;
		}
		return !valid;
	}

	private static void validateGBoardCount(Date date, List<PlanForm> createPlanList, List<Stock> stocks, Long gStockId,
			String specs, List<String> globalErrorList) {
		Long formTotalGBoardCount = calculateTotalGBoardCount(createPlanList, gStockId);
		Long planTotalGBoardCount = planRepository.sumGBoardCount(gStockId, DateUtil.getDateWithoutTime(date));
		Long actTotalGBoardCount = stockRepository.findById(gStockId).map(stock -> {
			Integer boardCount = stock.getBoardCount();
			return boardCount != null ? boardCount.longValue() : 0L; // 默認值
		}).orElse(0L);

		if (Long.sum(formTotalGBoardCount, planTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.gPosition.limit1",
					new Object[] { specs, findStockById(stocks, gStockId).getPosition(), MAX_BOARD_COUNT }));
		}
		if (Long.sum(formTotalGBoardCount, actTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.gPosition.limit2",
					new Object[] { specs, findStockById(stocks, gStockId).getPosition(), MAX_BOARD_COUNT }));
		}
	}

	private static void validatePBoardCount(Date date, List<PlanForm> createPlanList, List<Stock> stocks, Long pStockId,
			String specs, List<String> globalErrorList) {
		Long formTotalPBoardCount = calculateTotalPBoardCount(createPlanList, pStockId);
		Long planTotalPBoardCount = planRepository.sumPBoardCount(pStockId, DateUtil.getDateWithoutTime(date));
		Long actTotalPBoardCount = stockRepository.findById(pStockId).map(stock -> {
			Integer boardCount = stock.getBoardCount();
			return boardCount != null ? boardCount.longValue() : 0L; // 默認值
		}).orElse(0L);

		if (Long.sum(formTotalPBoardCount, planTotalPBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.pPosition.limit1",
					new Object[] { specs, findStockById(stocks, pStockId).getPosition(), MAX_BOARD_COUNT }));
		}
		if (Long.sum(formTotalPBoardCount, actTotalPBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
			globalErrorList.add(messageService.getMessage("plan.createForm.pPosition.limit2",
					new Object[] { specs, findStockById(stocks, pStockId).getPosition(), MAX_BOARD_COUNT }));
		}
	}

	private static Long calculateTotalGBoardCount(List<PlanForm> createPlanList, Long gStockId) {
		Long totalGBoardCount = 0L;
		for (PlanForm form : createPlanList) {
			if (form.getGStockId().equals(gStockId)) {
				totalGBoardCount = Long.sum(form.getGrowingBoardCount(), totalGBoardCount);
			}
		}
		return totalGBoardCount;
	}

	private static Long calculateTotalPBoardCount(List<PlanForm> createPlanList, Long pStockId) {
		Long totalPBoardCount = 0L;
		for (PlanForm form : createPlanList) {
			if (form.getPStockId().equals(pStockId)) {
				totalPBoardCount = Long.sum(form.getMatureBoardCount(), totalPBoardCount);
			}
		}
		return totalPBoardCount;
	}

	private static Stock findStockById(List<Stock> stocks, Long stockId) {
		try {
			return stocks.stream().filter(stock -> stock.getId().equals(stockId)).findFirst()
					.orElseThrow(() -> new Exception("Product not found"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Product findProductById(List<Product> products, Long productId) {
		try {
			return products.stream().filter(product -> product.getId().equals(productId)).findFirst()
					.orElseThrow(() -> new Exception("Product not found"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
