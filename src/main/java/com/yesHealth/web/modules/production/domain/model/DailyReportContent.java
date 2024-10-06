package com.yesHealth.web.modules.production.domain.model;

public enum DailyReportContent {
	Seed("播種日報表", "applicatoin/excel",
			new String[] { "序", "工單號碼", "品名", "播種盤數", "播種片數", "播種日期", "播種人數", "播種時間起", "壓水時間止", "使用前克數", "使用後克數", "播數",
					"工時預估", "備註" }),
	Water("壓水日報表", "applicatoin/excel",
			new String[] { "序", "工單號碼", "品名", "壓水盤數", "壓水人數", "壓水時間起", "壓水時間止", "暗房儲位", "暗移見日期", "備註" }),
	HeadOut("壓水日報表", "applicatoin/excel", new String[] { "序", "工單號碼", "品名", "計畫盤數", "計畫儲位", "實際盤數", "實際儲位", "開燈確認",
			"水道無水確認", "實際人數", "實際時間起", "實際時間止", "育苗日期", "備註" });

	private DailyReportContent(String type, String fileType, String[] header) {
		this.type = type;
		this.fileType = fileType;
		this.header = header;
	}

	private String type;
	private String fileType;
	private String[] header;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}

}
