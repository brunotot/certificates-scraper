package model;

public class ExcelDownloadObject {

	private String letter;
	
	private String keyNumber;
	
	private String yearNumber;
	
	private String indexNumber;
	
	private String code;

	public String getLetter() {
		return letter.toUpperCase();
	}

	public String getKeyNumber() {
		return keyNumber;
	}

	public String getYearNumber() {
		return yearNumber;
	}

	public String getIndexNumber() {
		return indexNumber;
	}

	public String getCode() {
		return code;
	}

	public void setLetter(String letter) {
		this.letter = letter.toUpperCase();
	}

	public void setKeyNumber(String keyNumber) {
		this.keyNumber = keyNumber;
	}

	public void setYearNumber(String yearNumber) {
		this.yearNumber = yearNumber;
	}

	public void setIndexNumber(String indexNumber) {
		this.indexNumber = indexNumber;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ExcelDownloadObject(String letter, String keyNumber, String year, String indexNumber, String code) {
		super();
		this.letter = letter;
		this.keyNumber = keyNumber;
		this.yearNumber = year;
		this.indexNumber = indexNumber;
		this.code = code;
	}
	
}
