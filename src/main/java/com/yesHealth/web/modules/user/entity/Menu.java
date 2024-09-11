package com.yesHealth.web.modules.user.entity;

import java.util.Set;

import javax.persistence.*;

import lombok.Data;

@Entity(name = "menu")
@Data
public class Menu {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String url;
	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Menu parent;

	@OneToMany(mappedBy = "parent")
	private Set<Menu> children;
	private String status;
	private Integer order;
}
