package com.yesHealth.web.modules.util.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.util.entity.FileUploadRecords;

public interface FileUploadRecordsRepository extends JpaRepository<FileUploadRecords, Long> {

}
