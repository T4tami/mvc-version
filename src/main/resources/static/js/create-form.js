function addRow() {
	// 獲取表單區域和模板行
	const formRows = document.getElementById('form-rows');
	const templateRow = document.getElementById('template-row');

	// 複製模板行
	const newRow = templateRow.cloneNode(true);

	// 取得新的rowId，根據現有的行數
	const rowId = formRows.childElementCount;

	// 更新newRow的ID
	newRow.id = `row-${rowId}`;

	// 查找newRow中的所有元素，更新id、name和for屬性
	newRow.querySelectorAll('[id], [name], [for],[data-row-id]').forEach(element => {
		if (element.id) {
			element.id = element.id.replace('-0', `-${rowId}`);
		}
		if (element.name) {
			element.name = element.name.replace('[0]', `[${rowId}]`);
		}
		if (element.htmlFor) {
			element.htmlFor = element.htmlFor.replace('-0', `-${rowId}`);
		}
		if (element.getAttribute('data-row-id')) {
			element.setAttribute('data-row-id', rowId);
		}

	});

	// 顯示刪除按鈕
	const removeButton = newRow.querySelector('button');
	removeButton.classList.remove('d-none');
	removeButton.dataset.rowId = rowId;

	// 將新的行插入表單中
	formRows.appendChild(newRow);
	const productId = document.getElementById(`product-id-${rowId}`);
	const targetWeight = document.getElementById(`target-weight-${rowId}`);
	const sStockId = document.getElementById(`s-stock-id-${rowId}`);
	const gStockId = document.getElementById(`g-stock-id-${rowId}`);
	const pStockId = document.getElementById(`p-stock-id-${rowId}`);
	const seedingDate = document.getElementById(`seeding-date-${rowId}`);
	const wateringDate = document.getElementById(`watering-date-${rowId}`);
	const headOutDate = document.getElementById(`head-out-date-${rowId}`);
	const growingDate = document.getElementById(`growing-date-${rowId}`);
	const matureDate = document.getElementById(`mature-date-${rowId}`);
	const harvestDate = document.getElementById(`harvest-date-${rowId}`);
	const seedingBoardCount = document.getElementById(`seeding-board-count-${rowId}`);
	const wateringBoardCount = document.getElementById(`watering-board-count-${rowId}`);
	const headOutBoardCount = document.getElementById(`head-out-board-count-${rowId}`);
	const growingBoardCount = document.getElementById(`growing-board-count-${rowId}`);
	const matureBoardCount = document.getElementById(`mature-board-count-${rowId}`);
	const harvestBoardCount = document.getElementById(`harvest-board-count-${rowId}`);
	productId.value = productId.options[0].value;
	targetWeight.value = 0;
	sStockId.value = sStockId.options[0].value;
	gStockId.value = gStockId.options[0].value;
	pStockId.value - pStockId.options[0].value;
	seedingDate.value = "";
	wateringDate.value = "";
	headOutDate.value = "";
	growingDate.value = "";
	matureDate.value = "";
	harvestDate.value = "";
	seedingBoardCount.value = 0;
	wateringBoardCount.value = 0;
	headOutBoardCount.value = 0;
	growingBoardCount.value = 0;
	matureBoardCount.value = 0;
	harvestBoardCount.value = 0;
}
function removeRow(button) {
	const rowId = button.dataset.rowId;
	const row = document.getElementById(`row-${rowId}`);
	if (row && rowId !== 'template-row') {
		row.remove(); // 刪除指定的行
	}

	// 重新對剩餘的行進行編號，跳過 template-row
	const formRows = document.getElementById('form-rows');
	const rows = formRows.querySelectorAll('.row'); // 選取所有行
	let rowIndex = 1; // 用於追蹤行編號

	rows.forEach(row => {
		if (row.id !== 'template-row') {
			// 重新設置行的ID
			row.id = `row-${rowIndex}`;

			// 查找行內的所有元素，更新id、name、for屬性
			row.querySelectorAll('[id],[name],[for],[data-row-id]').forEach(element => {
				if (element.id) {
					element.id = element.id.replace(/-\d+$/, `-${rowIndex}`);
				}
				if (element.name) {
					element.name = element.name.replace(/\[\d+\]/, `[${rowIndex}]`);
				}
				if (element.htmlFor) {
					element.htmlFor = element.htmlFor.replace(/-\d+$/, `-${rowIndex}`);
				}
				if (element.getAttribute('data-row-id')) {
					element.setAttribute('data-row-id', rowId);
				}
			});

			// 更新刪除按鈕的數據
			const removeButton = row.querySelector('button');
			removeButton.dataset.rowId = rowIndex;

			rowIndex++; // 更新下一行的索引
		}
	});
}

function handleChange(event) {
	// 取得觸發事件的 select 元素
	const selectElement = event.target;

	// 取得 select 元素所在的行 (data-row-id 屬性對應行)
	const rowId = selectElement.dataset.rowId;

	// 取得當前選中的選項
	const selectedOption = selectElement.options[selectElement.selectedIndex];

	// 取得各個 data-xx 屬性
	const specs = selectedOption.getAttribute('data-specs');
	const family = selectedOption.getAttribute('data-family');
	const sDays = selectedOption.getAttribute('data-sDays');
	const gDays = selectedOption.getAttribute('data-gDays');
	const pDays = selectedOption.getAttribute('data-pDays');
	const sHole = selectedOption.getAttribute('data-sHole');
	const gHole = selectedOption.getAttribute('data-gHole');
	const pHole = selectedOption.getAttribute('data-pHole');
	const sLux = selectedOption.getAttribute('data-sLux');
	const gLux = selectedOption.getAttribute('data-gLux');
	const pLux = selectedOption.getAttribute('data-pLux');
	const harvestStage = selectedOption.getAttribute('data-harveststage');

	// 找到對應的 p-stock-id-row 元素
	const pStockSelect = document.getElementById(`p-stock-id-${rowId}`);
	const matureDate = document.getElementById(`mature-date-${rowId}`);
	const matureBoardCount = document.getElementById(`mature-board-count-${rowId}`);

	const seedingDateInput = document.getElementById(`seeding-date-${rowId}`);
	const wateringDateInput = document.getElementById(`watering-date-${rowId}`);
	const headOutDateDateInput = document.getElementById(`head-out-date-${rowId}`);
	const growingDateInput = document.getElementById(`growing-date-${rowId}`);
	const matureDateInput = document.getElementById(`mature-date-${rowId}`);
	const harvestDateInput = document.getElementById(`harvest-date-${rowId}`);

	const today = new Date();
	let sDayNumber = parseInt(sDays, 10)
	let gDayNumber = parseInt(gDays, 10)
	let pDayNumber = parseInt(pDays, 10)
	// 如果 pLux 為 null 或空值，則禁用 p-stock-id-row，否則啟用
	if (harvestStage === 'G') {
		matureDate.disabled = true;
		pStockSelect.disabled = true;
		matureBoardCount.disabled = true;
		var newOption = document.createElement("option");
		newOption.value = 0;  // 设置 value
		newOption.text = "";     // 设置 text
		pStockSelect.add(newOption);  // 添加到下拉列表
		pStockSelect.value = 0;
		matureBoardCount.value = 0;
		//預設定案日期
		seedingDateInput.value = formatDate(today);
		wateringDateInput.value = formatDate(addDays(today, 1));
		headOutDateDateInput.value = formatDate(addDays(today, 2));
		growingDateInput.value = formatDate(addDays(today, 3 + sDayNumber));
		matureDateInput.value = "";
		harvestDateInput.value = formatDate(addDays(today, 3 + sDayNumber + gDayNumber));
	} else {
		matureDate.disabled = false;
		pStockSelect.disabled = false;
		matureBoardCount.disabled = false;
		for (var i = 0; i < pStockSelect.options.length; i++) {
			if (pStockSelect.options[i].value === "0") {
				pStockSelect.remove(i);
				break;
			}
		}

		var selectedIndex = pStockSelect.selectedIndex;
		//預設日期
		seedingDateInput.value = formatDate(today);
		wateringDateInput.value = formatDate(addDays(today, 1));
		headOutDateDateInput.value = formatDate(addDays(today, 2));
		growingDateInput.value = formatDate(addDays(today, 3 + sDayNumber));
		matureDateInput.value = formatDate(addDays(today, 3 + sDayNumber + gDayNumber));
		harvestDateInput.value = formatDate(addDays(today, 3 + sDayNumber + gDayNumber + pDayNumber));
	}
	//設定最小值
	seedingDate = seedingDateInput.value
	wateringDateInput.min = seedingDate;
	headOutDateDateInput.min = seedingDate;
	growingDateInput.min = seedingDate;
	matureDateInput.min = seedingDate;
	harvestDateInput.min = seedingDate;

	const tableContainer = document.getElementById("table-container");
	const mainTitle = document.getElementById("main-title");
	if (tableContainer.style.display === "none" || !tableContainer.style.display) {
		tableContainer.style.display = "block";
		//mainTitle
		mainTitle.style.top = "213px";
		mainTitle.style.position = "sticky";
		mainTitle.style.zIndex = 998;
		mainTitle.style.backgroundColor = "white";
		mainTitle.style.width = "100%";
		mainTitle.style.boxShadow = "0 2px 4px rgba(0, 0, 0, 0.1)";
	}

	// 更新對應的 <tbody> 行
	let tbodyRow = document.querySelector(`#data-table-body tr[data-id="${rowId}"]`);

	// 如果找到對應的行，則更新該行的各個 <td> 值，否則創建一個新行
	if (!tbodyRow) {
		// 創建新行
		tbodyRow = document.createElement('tr');
		tbodyRow.setAttribute('data-id', rowId);

		// 依次添加每一個單元格
		tbodyRow.innerHTML = `
		            <td>${specs || ''}</td>
		            <td>${family || ''}</td>
		            <td>${sDays || ''}</td>
		            <td>${gDays || ''}</td>
		            <td>${pDays || ''}</td>
		            <td>${sHole || ''}</td>
		            <td>${gHole || ''}</td>
		            <td>${pHole || ''}</td>
		            <td>${sLux || ''}</td>
		            <td>${gLux || ''}</td>
		            <td>${pLux || ''}</td>
		            <td>${harvestStage || ''}</td>
		        `;

		// 將新行插入表格
		document.getElementById("data-table-body").appendChild(tbodyRow);
	} else {
		// 更新現有的行
		tbodyRow.cells[0].textContent = specs || '';
		tbodyRow.cells[1].textContent = family || '';
		tbodyRow.cells[2].textContent = sDays || '';
		tbodyRow.cells[3].textContent = gDays || '';
		tbodyRow.cells[4].textContent = pDays || '';
		tbodyRow.cells[5].textContent = sHole || '';
		tbodyRow.cells[6].textContent = gHole || '';
		tbodyRow.cells[7].textContent = pHole || '';
		tbodyRow.cells[8].textContent = sLux || '';
		tbodyRow.cells[9].textContent = gLux || '';
		tbodyRow.cells[10].textContent = pLux || '';
		tbodyRow.cells[11].textContent = harvestStage || '';
	}
}
function updateMinDate(event) {
	let rowId = event.target.dataset.rowId;
	const seedingDateInput = document.getElementById(`seeding-date-${rowId}`);
	const wateringDateInput = document.getElementById(`watering-date-${rowId}`);
	const headOutDateDateInput = document.getElementById(`head-out-date-${rowId}`);
	const growingDateInput = document.getElementById(`growing-date-${rowId}`);
	const matureDateInput = document.getElementById(`mature-date-${rowId}`);
	const harvestDateInput = document.getElementById(`harvest-date-${rowId}`);

	const seedingDate = seedingDateInput.value;
	// 設定日期的最小值
	if (seedingDate) {
		wateringDateInput.value = '';
		headOutDateDateInput.value = '';
		growingDateInput.value = '';
		matureDateInput.value = '';
		harvestDateInput.value = '';
		wateringDateInput.min = seedingDate;
		headOutDateDateInput.min = seedingDate;
		growingDateInput.min = seedingDate;
		matureDateInput.min = seedingDate;
		harvestDateInput.min = seedingDate;
	} else {
		wateringDateInput.min = ''; // 清空最小值
		headOutDateDateInput.min = '';
		growingDateInput.min = '';
		matureDateInput.min = '';
		harvestDateInput.min = '';
	}
}
// 將 Date 轉換為 YYYY-MM-DD 格式的函數
function formatDate(date) {
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, '0'); // 月份從 0 開始
	const day = String(date.getDate()).padStart(2, '0');
	return `${year}-${month}-${day}`;
}

// 將日期加上天數的函數
function addDays(date, days) {
	const result = new Date(date);
	result.setDate(result.getDate() + days);
	return result;
}
function fillBoardCount(event) {
	// 取得觸發事件的 select 元素
	const selectElement = event.target;

	// 取得 select 元素所在的行 (data-row-id 屬性對應行)
	const rowId = selectElement.dataset.rowId;

	const productId = document.getElementById(`product-id-${rowId}`);
	const selectedOption = productId.options[productId.selectedIndex];
	console.log(productId.options[productId.selectedIndex])
	const estWeight = selectedOption.getAttribute('data-estWeight');
	const harvestStage = selectedOption.getAttribute('data-harvestStage');
	const targetWeight = selectElement.value * 1000;


	if (estWeight && harvestStage && productId && targetWeight) {
		const seedingBoardCount = document.getElementById(`seeding-board-count-${rowId}`);
		const wateringBoardCount = document.getElementById(`watering-board-count-${rowId}`);
		const headOutBoardCount = document.getElementById(`head-out-board-count-${rowId}`);
		const growingBoardCount = document.getElementById(`growing-board-count-${rowId}`);
		const matureBoardCount = document.getElementById(`mature-board-count-${rowId}`);
		const harvestBoardCount = document.getElementById(`harvest-board-count-${rowId}`);

		//採收栽培盤數

		harvestBoardCount.value = Math.ceil(targetWeight / estWeight);
		if (harvestStage != "G") {
			matureBoardCount.value = Math.ceil(targetWeight / estWeight);

		}
		//育苗盤數
		const pRate = selectedOption.getAttribute('data-pRate');
		const pHole = selectedOption.getAttribute('data-pHole');
		const gHole = selectedOption.getAttribute('data-gHole');
		growingBoardCount.value = Math.ceil(harvestBoardCount.value / pRate * pHole / gHole);

		//見苗盤數
		const gRate = selectedOption.getAttribute('data-gRate');
		const sHole = selectedOption.getAttribute('data-sHole');
		headOutBoardCount.value = Math.ceil(growingBoardCount.value / gRate * gHole / gHole);
		wateringBoardCount.value = Math.ceil(growingBoardCount.value / gRate * gHole / gHole);
		seedingBoardCount.value = Math.ceil(growingBoardCount.value / gRate * gHole / gHole);


	}
}