package com.yesHealth.web.modules.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "auth_role")
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String roleName;
	private String description;
	@ManyToMany(mappedBy = "roles")
	private List<UserEntity> users = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "auth_role_menu", joinColumns = {
			@JoinColumn(name = "role_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "menu_id", referencedColumnName = "id") })
	private List<Menu> menu = new ArrayList<>();

	@Column(name = "created_time", nullable = false)
	@CreatedDate
	private LocalDateTime createdTime;

	@Column(name = "updated_time", nullable = false)
	@LastModifiedDate
	private LocalDateTime updatedTime;

	@Column(name = "created_by", nullable = false)
	@CreatedBy
	private Long createdBy;

	@Column(name = "updated_by", nullable = false)
	@LastModifiedBy
	private Long updatedBy;
}
