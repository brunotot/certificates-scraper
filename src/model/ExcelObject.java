package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import iec.Utility;

public class ExcelObject {

	private String year;
	
	private String lastValidCertificate;
	
	private String lastNumberFromCertificates;
	
	private List<String> napomene;
	
	private List<String> missingCertificates;
	
	private List<String> repeatingCertificates;

	private String letter;

	private String keyNumber;

	public List<String> getNapomene() {
		return napomene;
	}

	public void setNapomene(List<String> napomene) {
		this.napomene = napomene;
	}

	public String getKeyNumber() {
		return keyNumber;
	}

	public void setKeyNumber(String keyNumber) {
		this.keyNumber = keyNumber;
	}

	public String getLetter() {
		return letter.toUpperCase();
	}

	public void setLetter(String letter) {
		this.letter = letter.toUpperCase();
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
	
	public String getLastValidCertificate() {
		if (lastValidCertificate == null || lastValidCertificate.isEmpty()) {
			return "/";
		}
		return Utility.formatNumberAsString(Integer.parseInt(lastValidCertificate));
	}

	public String getLastNumberFromCertificates() {
		if (lastNumberFromCertificates == null || lastNumberFromCertificates.isEmpty()) {
			return "/";
		}
		if (lastNumberFromCertificates.equals("nije pronadeno")) {
			return lastNumberFromCertificates;
		}
		return Utility.formatNumberAsString(Integer.parseInt(lastNumberFromCertificates));
	}

	public List<String> getMissingCertificates() {
		return missingCertificates;
	}

	public List<String> getRepeatingCertificates() {
		return repeatingCertificates;
	}

	public void setLastValidCertificate(String lastValidCertificate) {
		this.lastValidCertificate = lastValidCertificate;
	}

	public void setLastNumberFromCertificates(String lastNumberFromCertificates) {
		this.lastNumberFromCertificates = lastNumberFromCertificates;
	}

	public void setMissingCertificates(List<String> missingCertificates) {
		this.missingCertificates = missingCertificates;
	}

	public void setRepeatingCertificates(List<String> repeatingCertificates) {
		this.repeatingCertificates = repeatingCertificates;
	}

	public String getMissingCertificatesAsString() {
		String str = Utility.formatNumbersInArray(missingCertificates);
		if (str.isEmpty()) {
			str = "/";
		}
		return str;
	}

	public String getRepeatingCertificatesAsString() {
		String str = Utility.formatNumbersInArray(repeatingCertificates);
		if (str.isEmpty()) {
			str = "/";
		}
		return str;
	}
	
	public String getNapomeneAsString() {
		String str = Utility.formatNumbersInArray(napomene);
		if (str.isEmpty()) {
			str = "/";
		}
		return str;
	}
	
	public ExcelObject(XSSFSheet sheet, String lastNumberFromCertificates, String letter, String keyNumber, String year) throws Exception {
		super();
		this.letter = letter;
		this.keyNumber = keyNumber;
		this.year = year;
		this.lastNumberFromCertificates = lastNumberFromCertificates;
		this.lastValidCertificate = "";
		this.missingCertificates = new ArrayList<>();
		this.repeatingCertificates = new ArrayList<>();
		this.napomene = new ArrayList<>();
		
	    int rows = sheet.getPhysicalNumberOfRows();
	    if (rows == 1) {
	    	this.lastNumberFromCertificates = "/";
	    	this.lastValidCertificate = "/";
	    }
	    List<ExcelDownloadObject> excelDownloadObjects = new ArrayList<>();
	    for (int i = 1; i < rows; i++) {
	    	Row row = sheet.getRow(i);
	    	String certifikat = row.getCell(1).toString();
	    	String[] certifikatParams = certifikat.split("_");
	    	if (certifikatParams.length == 5) {
	    		String indexNumber = certifikatParams[3];
	    		String code = certifikatParams[4];
	    		excelDownloadObjects.add(new ExcelDownloadObject(letter, keyNumber, year, indexNumber, code));
	    	}
	    }
	    
	    Collections.sort(excelDownloadObjects, new Comparator<ExcelDownloadObject>() {
	        @Override
	        public int compare(ExcelDownloadObject o1, ExcelDownloadObject o2) {
	        	Integer indexNumber1 = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(o1.getIndexNumber()));
	        	Integer indexNumber2 = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(o2.getIndexNumber()));
	        	int c = indexNumber1.compareTo(indexNumber2);
	        	if (c == 0) {
	        		Integer year1 = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(o1.getYearNumber()));
	        		Integer year2 = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(o2.getYearNumber()));
	        		c = year1.compareTo(year2);
	        	}
	        	return c;
	        }
	    });

	    List<String> validObjectsStrings = new ArrayList<>();
	    Map<String, Integer> counterMap = new HashMap<>();
	    String lastValidCertificate = "001";
	    for (ExcelDownloadObject o : excelDownloadObjects) {
	    	int lastValidCertificateInteger = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(lastValidCertificate));
	    	String currentCertificate = Utility.formatNumberAsString(Integer.parseInt(Utility.removeTrailingNonDigitCharacters(o.getIndexNumber())));
	    	int currentCertificateInteger = Integer.parseInt(Utility.removeTrailingNonDigitCharacters(currentCertificate));
	    	if (currentCertificateInteger - lastValidCertificateInteger >= 100 || !o.getYearNumber().equals(year) || currentCertificateInteger == 0) {
	    		napomene.add(currentCertificate);
	    	} else {
	    		Integer count = counterMap.get(currentCertificate);
	    		if (count == null) {
	    			counterMap.put(currentCertificate, 1);
	    		} else {
	    			this.repeatingCertificates.add(currentCertificate);
	    		}
	    		lastValidCertificate = currentCertificate;
	    		validObjectsStrings.add(currentCertificate);
	    	}
	    }
	    this.lastValidCertificate = lastValidCertificate;
	    this.repeatingCertificates = new ArrayList<>(new TreeSet<>(this.repeatingCertificates));
	    
	    int i = 0;
	    int certificateIndex = 0;
	    List<String> validObjectsStringsDistinct = new ArrayList<>(new TreeSet<>(validObjectsStrings));
	    while (certificateIndex < validObjectsStringsDistinct.size()) {
	    	i++;
	    	String str = Utility.formatNumberAsString(i);
	    	String validObjectIndex = validObjectsStringsDistinct.get(certificateIndex);
	    	if (str.equals(validObjectIndex)) {
	    		certificateIndex++;
	    	} else {
	    		this.missingCertificates.add(str);
	    	}
	    }
	}
	
}
