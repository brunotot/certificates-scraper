package iec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.monitorjbl.xlsx.StreamingReader;

import model.ExcelObject;

public class Main {
	
	public static XSSFWorkbook wb = null;
	
	public static Log log = LogFactory.getLog(Main.class);

	private static Properties loadProperties(String propertiesPath) throws Exception {
		InputStream is = new FileInputStream("application.properties");
		Properties prop = new Properties();
		prop.load(is);
		return prop;
	}
	
	private static Map<String, String> getMappedCertificateValues(File fileToProcess, File certificatesFile) throws Exception {
		Map<String, Integer> map = new HashMap<>();
		StreamingReader reader = StreamingReader.builder()
		        .rowCacheSize(200000)
		        .bufferSize(4096)
		        .sheetIndex(0)
		        .read(certificatesFile);

		boolean start = true;
		for (Row row : reader) {
			if (start) {
				start = false;
				continue;
			}
			
			String[] params = row.getCell(0).getStringCellValue().split("_");
			if (params.length != 5) {
				continue;
			}
			String key = params[0].toUpperCase() + "_" + params[1] + "_" + params[2];
			Integer currentValue = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(params[3]));
			Integer mapValue = map.get(key);
			if (mapValue == null) {
				map.put(key, currentValue);
			} else {
				if (currentValue > mapValue) {
					map.put(key, currentValue);
				}
			}
		}
		reader.close();
		
		Map<String, String> certificates = new HashMap<>();
		wb = new XSSFWorkbook(fileToProcess);
		XSSFSheet sheet = wb.getSheetAt(0);
		for (Row row : sheet) {
			String keyUnformatted = row.getCell(0).getStringCellValue();
			String[] params = keyUnformatted.split("(\\-)|(/)");
			if (params.length == 3) {
				String keyFormatted = params[0].toUpperCase() + "_" + params[1] + "_" + params[2];
				Integer certificatesValue = map.get(keyFormatted);
				if (certificatesValue == null) {
					certificates.put(keyFormatted, "nije pronadeno");
				} else {
					certificates.put(keyFormatted, Utility.formatNumberAsString(certificatesValue));
				}
			}
		}
		return certificates;
	}
	
	public static void main(String[] args) {
		File fileToProcess = null;
		File certificatesFile = null;
		File outputFolder = null;
		
		try {
			log.info("Application started at " + new Date().toString() + ".");
			Properties prop = loadProperties("application.properties");

			String fileToProcessPath = prop.getProperty("fileToProcess");
			String certificatesFilePath = prop.getProperty("certificatesFile");
			String outputFolderPath = prop.getProperty("outputFolder");
			String username = prop.getProperty("username");
			String password = prop.getProperty("password");

			fileToProcess = new File(fileToProcessPath);
			certificatesFile = new File(certificatesFilePath);
			outputFolder = new File(outputFolderPath);
			
			if (!fileToProcess.exists() || !fileToProcess.getName().endsWith(".xlsx")) {
				log.error("Given fileToProcess path is invalid. Check fileToProcess parameter in application.properties");
				return;
			} else if (!certificatesFile.exists() || !certificatesFile.getName().endsWith(".xlsx")) {
				log.error("Given certificatesFile path is invalid. Check certificatesFile parameter in application.properties");
				return;
			} else if (!outputFolder.exists() || !outputFolder.isDirectory()) {
				log.error("Given outputFolder path is invalid. Check outputFolder parameter in application.properties");
				return;
			}
			
			IecWebsite iec = IecWebsite.getInstance();
			if (!iec.login(username, password)) {
				log.error("Invalid username or password. Check application.properties");
				return;
			}

			Map<String, String> certificates = getMappedCertificateValues(fileToProcess, certificatesFile);
			if (certificates.isEmpty()) {
				log.error("An error occurred while fetching all certificates");
				return;
			}

			log.info("Application started at " + new Date().toString() + ".");
			XSSFSheet sheet = wb.getSheetAt(0);
			int len = sheet.getPhysicalNumberOfRows();
			for (int i = 0; i < len; i++) {
				Row row = sheet.getRow(i);
				String[] oznakaCertifikataParams = row.getCell(0).toString().split("(/)|(\\-)");
		    	String letter = oznakaCertifikataParams[0].toUpperCase();
		    	String keyNumber = oznakaCertifikataParams[1];
		    	String year = oznakaCertifikataParams[2];
		    	String oznakaCertifikata = letter + "_" + keyNumber + "_";
		    	String certificatesKey = letter + "_" + keyNumber + "_" + year;
		    	log.info(i + ". " + letter + "_" + keyNumber + "_" + year + " start...");
		    	File excelValueFile = iec.downloadExcelValueFile(oznakaCertifikata);
		    	if (excelValueFile == null) {
			    	row.getCell(4).setCellValue("/");
			    	row.getCell(5).setCellValue("/");
			    	row.getCell(6).setCellValue("/");
			    	row.getCell(7).setCellValue("/");
			    	row.getCell(8).setCellValue("/");
			    	row.getCell(9).setCellValue("/");
		    	} else {
			    	String lastNumberFromCertificates = certificates.get(certificatesKey);
		    		XSSFWorkbook wb = new XSSFWorkbook(excelValueFile);
		    		XSSFSheet sheetExcelFile = wb.getSheetAt(0);
		    		wb.close();
		    		ExcelObject excelObject = new ExcelObject(sheetExcelFile, lastNumberFromCertificates, letter, keyNumber, year);
		    		row.getCell(4).setCellValue(excelObject.getLastValidCertificate());
		    		row.getCell(5).setCellValue(excelObject.getLastNumberFromCertificates());
		    		row.getCell(6).setCellValue("/");
		    		row.getCell(7).setCellValue(excelObject.getMissingCertificatesAsString());
		    		row.getCell(8).setCellValue(excelObject.getRepeatingCertificatesAsString());
		    		row.getCell(9).setCellValue(excelObject.getNapomeneAsString());
		    	}
			}
			
			log.info("Application successfully finished at " + new Date().toString() + ".");
		} catch (Exception e) {
			log.error("Application finished with errors. Check logs!", e);
		} finally {
			if (wb != null) {
		    	try {
					String[] fileNameParams = fileToProcess.getName().split("\\.");
					String onlyFileName = fileNameParams[0];
					String onlyFileExtension = fileNameParams[1];
					SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy_HH_mm"); 
		    		File out = new File(outputFolder.getAbsoluteFile() + File.separator + onlyFileName + "_" + format.format(new Date()) + "." + onlyFileExtension);
					if (out.exists()) {
						out.delete();
					} 
					out.createNewFile();
					FileOutputStream fileOut = new FileOutputStream(out);
		    		wb.write(fileOut);
		    		fileOut.close();
					wb.close();
					log.info("Written results to " + out.getAbsolutePath());
		    	} catch (Exception e) {
		    		log.error("An error occurred while writing result to file", e);
		    	}
			}
		}
	}
}
