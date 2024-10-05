package com.yesHealth.web.modules.production.domain.respository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yesHealth.web.modules.product.domain.entity.TransplantRecord;

public interface TransplantRecordRepository extends JpaRepository<TransplantRecord, Long> {
	@Query(nativeQuery = true, value = "SELECT COALESCE(SUM(tr.ACT_BOARD_COUNT),0) FROM TRANSPLANT_RECORD tr "
			+ "LEFT JOIN PRODUCT_SCHEDULE ps ON tr.PS_ID=ps.ID "
			+ "WHERE tr.STOCK_ID = :STOCK_ID AND CONVERT(DATE,tr.ACT_TRANS_DATE) >= CONVERT(DATE,:ACT_TRANS_DATE) "
			+ "AND CONVERT(DATE,ps.MATURE_DATE) <= CONVERT(DATE,GETDATE()) AND tr.STAGE = :STAGE")
	Long sumActGBoardCountByStage(@Param("STOCK_ID") Long stockId, @Param("ACT_TRANS_DATE") Date actTransDate,
			@Param("STAGE") String stage);

	@Query(nativeQuery = true, value = "SELECT COALESCE(SUM(tr.ACT_BOARD_COUNT),0) FROM TRANSPLANT_RECORD tr "
			+ "LEFT JOIN PRODUCT_SCHEDULE ps ON tr.PS_ID=ps.ID "
			+ "WHERE tr.STOCK_ID = :STOCK_ID AND CONVERT(DATE,tr.ACT_TRANS_DATE) >= CONVERT(DATE,:ACT_TRANS_DATE) "
			+ "AND CONVERT(DATE,ps.HARVEST_DATE) <= CONVERT(DATE,GETDATE()) AND tr.STAGE = :STAGE")
	Long sumActPBoardCountByStage(@Param("STOCK_ID") Long stockId, @Param("ACT_TRANS_DATE") Date actTransDate,
			@Param("STAGE") String stage);
}
