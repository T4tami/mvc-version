/* 切換子菜單顯示的函數 */
function toggleSubMenu(event) {
	const submenu = event.currentTarget.nextElementSibling;
	if (submenu.style.display === "none" || submenu.style.display === "") {
		submenu.style.display = "flex";
		submenu.style.flexDirection = "column";
	} else {
		submenu.style.display = "none";
		submenu.style.flexDirection = ""; // Reset to default
	}
}

/* 切換左側菜單的收攏和展開狀態 */
function toggleSidebar() {
	const sidebar = document.querySelector(".sidebar");
	sidebar.classList.toggle("collapsed");
}