package com.yesHealth.web.modules.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
		File tmpDirectory = new File(UPLOAD_TMP_DIR);
		if (!tmpDirectory.exists()) {
			tmpDirectory.mkdirs(); // 創建目錄
		}
		String originalFileName = uploadFile.getOriginalFilename();
		String extension = "";

		if (originalFileName != null && originalFileName.lastIndexOf(".") > 0) {
			extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
		}
		Date date = new Date();
		String newFileName = originalFileName + "_" + date.getTime() + extension;
		File tmpFile = new File(tmpDirectory, newFileName);
		try {
			uploadFile.transferTo(tmpFile);
		} catch (IllegalStateException | IOException e) {
			log.error("username :" + "upload falied" + ",filename=" + newFileName);
			e.printStackTrace();
		}
		return tmpFile;
	}

	public static File saveToDisk(File file) throws UplaodFileException, FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException("查無此檔案");
		}
		if (file.length() == 0) {
			throw new IllegalArgumentException("File is empty.");
		}
		File directory = new File(UPLOAD_DIR);
		if (!directory.exists()) {
			directory.mkdirs(); // 創建目錄
		}

		File destinationFile = new File(directory, file.getName());
		try {
			Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("username: upload failed, filename=" + file.getName());
			throw new UplaodFileException("文件上傳失敗", e);
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
				List<String> rowData = rowList.get(i);

				boolean hasData = false;
				for (int j = 1; j < rowData.size(); j++) { // 假設序號在第一欄
					if (rowData.get(j) != null && !rowData.get(j).isEmpty()) {
						hasData = true;
						break;
					}
				}

				if (hasData) {
					Map<String, String> dataMap = new HashMap<>();
					for (int j = 0; j < expectedHeader.length; j++) {
						dataMap.put(expectedHeader[j], j < rowData.size() ? rowData.get(j) : null);
					}
					dataList.add(dataMap);
				}
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
