package iec;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {

	public static long LONG_PAGE_WAIT = 30000;
	
	public static String formatNumberAsString(Integer num) {
		if (num < 1000) {
			return String.format("%03d", num);
		} else if (num < 10000) {
			return String.format("%04d", num);
		} else {
			return String.format("%05d", num);
		}
	}
	
	public static String removeTrailingNonDigitCharacters(String str) {
		String newString = str;
		while (newString.charAt(0) == '\n' || newString.charAt(0) == ' ' || newString.charAt(0) == '\t' || !Character.isDigit(newString.charAt(0))) {
			if (newString.length() == 1) {
				newString = "";
				break;
			}
			newString = newString.substring(1);
		}
		
		while (newString.charAt(newString.length() - 1) == '\n' || newString.charAt(newString.length() - 1) == ' ' || newString.charAt(newString.length() - 1) == '\t' || !Character.isDigit(newString.charAt(newString.length() - 1))) {
			if (newString.length() == 1) {
				newString = "";
				break;
			}
			newString = newString.substring(0, newString.length() - 1);
		}
		return newString;
	}
	
	public static File getLatestFilefromDir(String dirPath){
	    File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    if (files == null || files.length == 0) {
	        return new File("placeholder name");
	    }

	    File lastModifiedFile = files[0];
	    for (int i = 1; i < files.length; i++) {
	       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	       }
	    }
	    
	    return lastModifiedFile;
	}

	public static String formatNumbersInArray(List<String> missingCertificates) {
		List<String> missingCertificatesDistinct = missingCertificates.stream().distinct().collect(Collectors.toList());
		String result = "";
		if (missingCertificatesDistinct.size() > 0) {            
	        int i = 0;
	        int j = 0; 
	        do {
	            j = i + 1;
	            while (j < missingCertificatesDistinct.size()) {
	                if (Integer.parseInt(missingCertificatesDistinct.get(j)) - Integer.parseInt(missingCertificatesDistinct.get(i)) != j - i) {
	                    break;
	                }
	                j++;
	            }
	            if (i == j - 1) {
	                result += missingCertificatesDistinct.get(i) + ", ";
	            } else {
	                result += "[" + missingCertificatesDistinct.get(i) + "-" + missingCertificatesDistinct.get(j - 1) + "], ";
	            }
	            i = j;
	        } while(i < missingCertificatesDistinct.size());
		}
		if (result.length() > 2) {
		    result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	public static File awaitForNewFile(File currentLatestFile, String dirPath) throws Exception {
		File newFile;
		int counter = 0;
		while (true) {
			Thread.sleep(100);
			counter++;
			newFile = getLatestFilefromDir(dirPath);
			if (!newFile.equals(currentLatestFile) && !newFile.getName().endsWith(".crdownload") && !newFile.getName().endsWith(".tmp")) {
				break;
			}
			if (counter == 300) {
				break;
			}
		}
		return newFile;
	}
	
}
