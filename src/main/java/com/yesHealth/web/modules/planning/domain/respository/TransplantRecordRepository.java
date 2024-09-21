package com.yesHealth.web.modules.planning.domain.respository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.product.domain.entity.TransplantRecord;

public interface TransplantRecordRepository extends JpaRepository<TransplantRecord, Long> {
	@Query("SELECT COALESCE(SUM(tr.actBoardCount),0) FROM TransplantRecord tr " + "LEFT JOIN tr.productSchedule ps "
			+ "WHERE tr.stock = :stock_id AND tr.actTransDate > :act_Trans_Date "
			+ "AND ps.matureDate < :mature_Date AND tr.stage = :stage")
	Long sumActGBoardCountByStage(@Param("stock_id") Stock stockId, @Param("act_Trans_Date") Date actTransDate,
			@Param("mature_Date") Date matureDate, @Param("stage") String stage);

	@Query("SELECT COALESCE(SUM(tr.actBoardCount),0) FROM TransplantRecord tr " + "LEFT JOIN tr.productSchedule ps "
			+ "WHERE tr.stock = :stock_id AND tr.actTransDate > :act_Trans_Date "
			+ "AND ps.harvestDate < :harvest_Date AND tr.stage = :stage")
	Long sumActPBoardCountByStage(@Param("stock_id") Stock stockId, @Param("act_Trans_Date") Date actTransDate,
			@Param("harvest_Date") Date harvestDate, @Param("stage") String stage);
}
