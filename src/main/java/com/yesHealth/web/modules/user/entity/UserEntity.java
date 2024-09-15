package com.yesHealth.web.modules.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

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
@Entity(name = "auth_user")
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	private String name;
	private String email;
	private String password;
	private String imgName;
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "auth_user_role", joinColumns = {
			@JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "role_id", referencedColumnName = "id") })
	private List<Role> roles = new ArrayList<>();
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
