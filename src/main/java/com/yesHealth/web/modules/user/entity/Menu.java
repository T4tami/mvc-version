package com.yesHealth.web.modules.user.entity;

import java.util.List;
import java.util.Set;

import javax.persistence.*;

import lombok.Data;

@Entity(name = "auth_menu")
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
	private List<Menu> children;
	private String status;
	private Long orderSn;

	@Override
	public int hashCode() {
		return (id != null) ? id.hashCode() : super.hashCode();
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj == null || getClass() != obj.getClass()) {
//			return false;
//		}
//		Menu other = (Menu) obj;
//		return (id != null) && id.equals(other.getId());
//	}
}
