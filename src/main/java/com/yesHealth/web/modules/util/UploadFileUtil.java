package com.yesHealth.web.modules.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.yesHealth.web.modules.util.exception.UplaodFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UploadFileUtil {
	private static final String UPLOAD_DIR = "C:/opt/yh/upload/dailyReport";
	private static final String UPLOAD_TMP_DIR = "C:/opt/yh/upload/tmp";

	public static File saveToTmpDisk(MultipartFile uploadFile) throws UplaodFileException {
		if (uploadFile.getSize() == 0) {
			throw new UplaodFileException("請確認上傳檔案，檔案不可為空");
		}
		File directory = new File(UPLOAD_TMP_DIR);
		if (!directory.exists()) {
			directory.mkdirs(); // 創建目錄
		}
		Date date = new Date();
		String formatFileName = uploadFile.getOriginalFilename() + "_" + date.getTime();
		File destinationFile = new File(directory, formatFileName);
		try {
			uploadFile.transferTo(destinationFile);
		} catch (IllegalStateException | IOException e) {
			log.error("username :" + "upload falied" + ",filename=" + formatFileName);
			e.printStackTrace();
		}
		return destinationFile;
	}

	public static List<Map<String, String>> readExcelFile(File file, String[] expectedHeader)
			throws UplaodFileException {
		List<Map<String, String>> dataList = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis);) {
			Sheet sheet = workbook.getSheetAt(0);

			List<List<String>> rowList = new ArrayList<>();
			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				List<String> cellList = new ArrayList<>();
				for (Cell cell : row) {
					String cellValue = cell.toString();
					cellList.add(cellValue);
				}
				rowList.add(cellList);
			}
			// 找出header的rowNum
			int headerRowIndex = -1;
			for (int i = 0; i < rowList.size(); i++) {
				if (Arrays.equals(rowList.get(i).toArray(new String[0]), expectedHeader)) {
					headerRowIndex = i;
					break;
				}
			}
			// 如果未找到表頭，拋出例外
			if (headerRowIndex == -1) {
				throw new UplaodFileException("未找到預期的表頭");
			}
			// 根據表頭索引填充 dataList
			for (int i = headerRowIndex + 1; i < rowList.size(); i++) {
				Map<String, String> dataMap = new HashMap<>();
				List<String> rowData = rowList.get(i);
				for (int j = 0; j < expectedHeader.length; j++) {
					dataMap.put(expectedHeader[j], j < rowData.size() ? rowData.get(j) : null);
				}
				dataList.add(dataMap);
			}
		} catch (FileNotFoundException e) {
			log.error("file not found");
			e.printStackTrace();
			throw new UplaodFileException("檔案找不到，請重新上傳");
		} catch (IOException e) {
			e.printStackTrace();
			throw new UplaodFileException("讀取檔案時出現錯誤");
		}
		return dataList;
	}
}
