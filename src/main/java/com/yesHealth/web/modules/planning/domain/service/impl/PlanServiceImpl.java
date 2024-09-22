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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.planning.domain.respository.PlanRepository;
import com.yesHealth.web.modules.planning.domain.respository.StockRepository;
import com.yesHealth.web.modules.planning.domain.respository.TransplantRecordRepository;
import com.yesHealth.web.modules.planning.domain.service.OrderService;
import com.yesHealth.web.modules.planning.domain.service.PlanService;
import com.yesHealth.web.modules.planning.web.views.CreatePlanForm;
import com.yesHealth.web.modules.planning.web.views.CreatePlansForm;
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
	private static final String ERR_KEY_PREFIX = "createPlansForm.createPlanFormList";

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
		Page<ProductSchedule> plans = planRepository.findAll(pageable);
		return plans;
	}

	@Override
	public Page<ProductSchedule> findByestHarvestDateBetween(String startDateStr, String endDateStr,
			Pageable pageable) {
		Date formateStartDate = startDateStr == null ? getStartOfNextWeek() : convertStringToDate(startDateStr);
		Date formateEndDate = endDateStr == null ? getEndOfNextWeek() : convertStringToDate(endDateStr);
		return planRepository.findByHarvestDateBetween(formateStartDate, formateEndDate, pageable);
	}

	@Override
	public Map<String, Object> validateCreatePlan(CreatePlansForm createPlansForm) {
		List<CreatePlanForm> createPlanList = createPlansForm.getCreatePlanFormList();
		List<Stock> stocks = stockRepository.findAll();
		List<Product> products = productRepository.findAll();
		Map<String, Object> businessErrors = new HashMap<>();
		List<String> globalErrorList = new ArrayList<>();
		int index = 0;
		for (CreatePlanForm createPlanForm : createPlanList) {
			Long productId = createPlanForm.getProductId();
			Long sStockId = createPlanForm.getSStockId();
			Long gStockId = createPlanForm.getGStockId();
			Long pStockId = createPlanForm.getPStockId();

			Product product = findProductById(products, productId);
			String harvestStage = product.getHarvestStage();
			// 製程照度驗證
			int productSLux = Integer.parseInt(product.getSLux());
			int productGLux = Integer.parseInt(product.getGLux());
			int productPLux = !"G".equals(harvestStage) ? Integer.parseInt(product.getPLux()) : 0;

			int stockSLux = findLuxByStockId(stocks, sStockId);
			int stockGLux = findLuxByStockId(stocks, gStockId);
			int stockPLux = (!"G".equals(harvestStage)) ? findLuxByStockId(stocks, pStockId) : 0;
			String specs = product.getSpecs();

			if (!validateLux(productSLux, stockSLux)) {
				String err = messageService.getMessage("plan.createForm.stockSLux.error",
						new Object[] { specs, productSLux });
				businessErrors.put(ERR_KEY_PREFIX + "[" + index + "]." + "sStockId", err);
			}

			if (!validateLux(productGLux, stockGLux)) {
				String err = messageService.getMessage("plan.createForm.stockGLux.error",
						new Object[] { specs, productGLux });
				businessErrors.put(ERR_KEY_PREFIX + "[" + index + "]." + "gStockId", err);
			}

			if (!"G".equals(harvestStage)) {
				if (productPLux == 340) {
					if (!validateLux(productPLux, stockPLux)) {
						String err = messageService.getMessage("plan.createForm.stockPLux.error",
								new Object[] { specs, productPLux });
						businessErrors.put(ERR_KEY_PREFIX + "[" + index + "]." + "pStockId", err);
					}
				} else if (productPLux == 440) {
					if (stockPLux < productPLux) {
						String err = messageService.getMessage("plan.createForm.stockPLux.range.error",
								new Object[] { specs, productPLux });
						businessErrors.put(ERR_KEY_PREFIX + "[" + index + "]." + "pStockId", err);
					}
				}
			}
			// 製程天數驗證
			long actualSDays = daysDifference(createPlanForm.getGrowingDate(), createPlanForm.getHeadOutDate());
			Integer expectedSDays = product.getSDays();
			if (!validDays(expectedSDays, actualSDays)) {
				String err = messageService.getMessage("plan.createForm.sDays.error",
						new Object[] { specs, expectedSDays });
				globalErrorList.add(err);
			}

			if (!"G".equals(harvestStage)) {

				long actualGDays = daysDifference(createPlanForm.getMatureDate(), createPlanForm.getGrowingDate());
				Integer expectedGDays = product.getGDays();
				if (!validDays(expectedGDays, actualGDays)) {
					String err = messageService.getMessage("plan.createForm.gDays.error",
							new Object[] { specs, expectedGDays });
					globalErrorList.add(err);
				}

				long actualPDays = daysDifference(createPlanForm.getHarvestDate(), createPlanForm.getMatureDate());
				Integer expectedPDays = product.getPDays();
				if (!validDays(expectedPDays, actualPDays)) {
					String err = messageService.getMessage("plan.createForm.pDays.error",
							new Object[] { expectedPDays });
					globalErrorList.add(err);
				}

			} else if ("G".equals(harvestStage)) {
				long actualGDays = daysDifference(createPlanForm.getHarvestDate(), createPlanForm.getGrowingDate());
				Integer expectedGDays = product.getGDays();
				if (!validDays(expectedGDays, actualGDays)) {
					String err = messageService.getMessage("plan.createForm.gType.gDays.error",
							new Object[] { specs, expectedGDays });
					globalErrorList.add(err);
				}
			}
			// 水道盤數驗證
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date growingDate = null, matureDate = null;
			try {

				if (!"G".equals(harvestStage)) {
					Long formTotalPBoardCount = Long.valueOf("0");
					Long formTotalGBoardCount = Long.valueOf("0");

					growingDate = format.parse(createPlanForm.getGrowingDate());
					matureDate = format.parse(createPlanForm.getMatureDate());
					for (CreatePlanForm form : createPlanList) {
						Long formProductId = form.getProductId();
						Product formProduct = findProductById(products, formProductId);
						String formHarvestStage = formProduct.getHarvestStage();
						if (form.getGStockId().equals(gStockId)) {
							formTotalGBoardCount = Long.sum(form.getGrowingBoardCount(), formTotalGBoardCount);

						}
						if (!"G".equals(formHarvestStage)) {
							if (form.getPStockId().equals(pStockId)) {
								formTotalPBoardCount = Long.sum(form.getMatureBoardCount(), formTotalPBoardCount);
							}
						}
					}
					// G水道
					Long planTotalGBoardCount = planRepository.sumGBoardCount(gStockId,
							getDateWithoutTime(growingDate));
					if (Long.sum(formTotalGBoardCount, planTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
						String gPosition = findStockById(stocks, gStockId).getPosition();
						String err = messageService.getMessage("plan.createForm.gPosition.limit1",
								new Object[] { specs, gPosition, MAX_BOARD_COUNT });
						globalErrorList.add(err);
					}
					Long actTotalGBoardCount = transplantRecordRepository.sumActGBoardCountByStage(gStockId,
							getDateWithoutTime(growingDate), "G");
					if (Long.sum(formTotalGBoardCount, actTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
						String gPosition = findStockById(stocks, gStockId).getPosition();
						String err = messageService.getMessage("plan.createForm.gPosition.limit2",
								new Object[] { specs, gPosition, MAX_BOARD_COUNT });
						globalErrorList.add(err);
					}
					// P水道
					Long planTotalPBoardCount = planRepository.sumPBoardCount(pStockId, getDateWithoutTime(matureDate));
					if (Long.sum(formTotalPBoardCount, planTotalPBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
						String pPosition = findStockById(stocks, pStockId).getPosition();
						String err = messageService.getMessage("plan.createForm.pPosition.limit1",
								new Object[] { specs, pPosition, MAX_BOARD_COUNT });
						globalErrorList.add(err);
					}
					Long actTotalPBoardCount = transplantRecordRepository.sumActPBoardCountByStage(pStockId,
							getDateWithoutTime(matureDate), "G");
					if (Long.sum(formTotalPBoardCount, actTotalPBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
						String pPosition = findStockById(stocks, pStockId).getPosition();
						String err = messageService.getMessage("plan.createForm.pPosition.limit2",
								new Object[] { specs, pPosition, MAX_BOARD_COUNT });
						globalErrorList.add(err);
					}
				} else if ("G".equals(harvestStage)) {
					growingDate = format.parse(createPlanForm.getGrowingDate());
					Long formTotalGBoardCount = Long.valueOf("0");
					for (CreatePlanForm form : createPlanList) {
						if (form.getGStockId().equals(gStockId)) {
							formTotalGBoardCount = Long.sum(form.getGrowingBoardCount(), formTotalGBoardCount);
						}
					}
					Long planTotalGBoardCount = planRepository.sumGBoardCountByG(gStockId,
							getDateWithoutTime(growingDate));
					if (Long.sum(formTotalGBoardCount, planTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
						String gPosition = findStockById(stocks, gStockId).getPosition();
						String err = messageService.getMessage("plan.createForm.gPosition.limit1",
								new Object[] { specs, gPosition, MAX_BOARD_COUNT });
						globalErrorList.add(err);
					}
					Long actTotalGBoardCount = transplantRecordRepository.sumActGBoardCountByStage(gStockId,
							getDateWithoutTime(growingDate), "G");
					if (Long.sum(formTotalGBoardCount, actTotalGBoardCount) > Long.valueOf(MAX_BOARD_COUNT)) {
						String gPosition = findStockById(stocks, gStockId).getPosition();
						String err = messageService.getMessage("plan.createForm.gPosition.limit2",
								new Object[] { specs, gPosition, MAX_BOARD_COUNT });
						globalErrorList.add(err);
					}
				}
			} catch (ParseException e) {
				log.error("日期格式錯誤，string轉換date失敗");
				e.printStackTrace();
			}
		}
		businessErrors.put("globalError", globalErrorList);
		return businessErrors;
	}

	private Stock findStockById(List<Stock> stocks, Long gStockId) {
		try {
			return stocks.stream().filter(stock -> stock.getId().equals(gStockId)).findFirst()
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

	@Override
	public void saveProductSchedule(CreatePlansForm createPlansForm) {
		List<Stock> stocks = stockRepository.findAll();
		List<Product> products = productRepository.findAll();
		int index = 0;
		for (CreatePlanForm createPlanForm : createPlansForm.getCreatePlanFormList()) {
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
						.seedingBoardCount(createPlanForm.getSeedingBoardCount())
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

	private Date getDateWithoutTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

}
