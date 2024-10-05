package com.yesHealth.web.modules.util.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yesHealth.web.modules.user.entity.UserEntity;

import lombok.Builder;

@Entity
@Table(name = "FileUploadRecords")
@Builder
public class FileUploadRecords {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fileName", nullable = false)
	private String fileName;

	@Column(name = "fileType")
	private String fileType;

	@Column(name = "fileSize")
	private Long fileSize;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity uploadedBy;

	@Column(name = "createTime")
	private Date createTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private FileUploadStatus status;

	@Column(name = "errorMessage")
	private String errorMessage;

	@Column(name = "description")
	private String description;
}
