package com.yesHealth.web.modules.production.domain.respository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

public interface PlanRepository extends JpaRepository<ProductSchedule, Long> {
	Page<ProductSchedule> findAll(Pageable pageable);

	Page<ProductSchedule> findAllByStatus(String status, Pageable pageable);

	Page<ProductSchedule> findByHarvestDateBetweenAndStatus(Date startDate, Date endDate, String Status,
			Pageable pageable);

	Page<ProductSchedule> findBySeedingDateBetweenAndStatus(Date startDate, Date endDate, String Status,
			Pageable pageable);

	Page<ProductSchedule> findByWateringDateBetweenAndStatus(Date startDate, Date endDate, String Status,
			Pageable pageable);

	Page<ProductSchedule> findByHeadOutDateBetweenAndStatus(Date startDate, Date endDate, String Status,
			Pageable pageable);

	List<ProductSchedule> findBySeedingDateBetweenAndStatus(Date startOfDay, Date endOfDay, String status);

	List<ProductSchedule> findByWateringDateBetweenAndStatus(Date startOfDay, Date endOfDay, String status);

	List<ProductSchedule> findByHeadOutDateBetweenAndStatus(Date startOfDay, Date endOfDay, String status);

	List<ProductSchedule> findByManuNo(String manuNo);

	@Query(value = "SELECT COUNT(*) FROM PRODUCT_SCHEDULE WHERE CONVERT(DATE, CREATE_DATE) = CONVERT(DATE, :CREATE_DATE)", nativeQuery = true)
	long countByCreateDate(@Param("CREATE_DATE") Date createDate);

	@Query(value = "SELECT COALESCE(SUM(ps.GROWING_BOARD_COUNT),0) FROM PRODUCT_SCHEDULE ps WHERE ps.G_STOCK_ID = :G_STOCK_ID AND CONVERT(DATE,ps.GROWING_DATE) >= CONVERT(DATE,:GROWING_DATE) AND CONVERT(DATE,ps.MATURE_DATE) >= CONVERT(DATE,GETDATE())", nativeQuery = true)
	Long sumGBoardCount(@Param("G_STOCK_ID") Long gStockId, @Param("GROWING_DATE") Date growingDate);

	@Query(value = "SELECT COALESCE(SUM(ps.MATURE_BOARD_COUNT),0) FROM PRODUCT_SCHEDULE ps WHERE ps.P_STOCK_ID = :P_STOCK_ID AND CONVERT(DATE,ps.MATURE_DATE) >= CONVERT(DATE,:MATURE_DATE) AND CONVERT(DATE,ps.HARVEST_DATE) >= CONVERT(DATE,GETDATE())", nativeQuery = true)
	Long sumPBoardCount(@Param("P_STOCK_ID") Long gStockId, @Param("MATURE_DATE") Date matureDate);

	@Query(value = "SELECT COALESCE(SUM(ps.GROWING_BOARD_COUNT),0) FROM PRODUCT_SCHEDULE ps WHERE ps.G_STOCK_ID = :G_STOCK_ID AND CONVERT(DATE,ps.GROWING_DATE) >= CONVERT(DATE,:GROWING_DATE) AND CONVERT(DATE,ps.HARVEST_DATE) >= CONVERT(DATE,GETDATE())", nativeQuery = true)
	Long sumGBoardCountByG(@Param("G_STOCK_ID") Long gStockId, @Param("GROWING_DATE") Date growingDate);

}
