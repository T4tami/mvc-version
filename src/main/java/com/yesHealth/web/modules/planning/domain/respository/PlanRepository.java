package com.yesHealth.web.modules.planning.domain.respository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.product.domain.entity.Stock;

public interface PlanRepository extends JpaRepository<ProductSchedule, Long> {
	Page<ProductSchedule> findAll(Pageable pageable);

	Page<ProductSchedule> findByHarvestDateBetween(Date startDate, Date endDate, Pageable pageable);

	long countByCreateDate(Date createDate);

	@Query("SELECT COALESCE(SUM(ps.growingBoardCount),0) FROM ProductSchedule ps WHERE ps.gStockId = :G_STOCK_ID AND ps.growingDate > :GROWING_DATE AND ps.matureDate < :MATURE_DATE")
	Long sumGBoardCount(@Param("G_STOCK_ID") Stock gStockId, @Param("GROWING_DATE") Date growingDate,
			@Param("MATURE_DATE") Date matureDate);

	@Query("SELECT COALESCE(SUM(ps.matureBoardCount),0) FROM ProductSchedule ps WHERE ps.pStockId = :P_STOCK_ID AND ps.matureDate > :MATURE_DATE AND ps.harvestDate < :HARVEST_DATE")
	Long sumPBoardCount(@Param("P_STOCK_ID") Stock gStockId, @Param("MATURE_DATE") Date matureDate,
			@Param("HARVEST_DATE") Date harvestDate);

	@Query("SELECT COALESCE(SUM(ps.growingBoardCount),0) FROM ProductSchedule ps WHERE ps.gStockId = :G_STOCK_ID AND ps.growingDate > :GROWING_DATE AND ps.harvestDate < :HARVEST_DATE")
	Long sumGBoardCountByG(@Param("G_STOCK_ID") Stock gStockId, @Param("GROWING_DATE") Date growingDate,
			@Param("HARVEST_DATE") Date harvestDate);

}
