package dk.statsbiblioteket.doms.licensemodule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseContent;

public class Util {

	private static final Logger log = LoggerFactory.getLogger(Util.class);

	// SimpleDateFormat is not thread-safe, so give one to each thread
	private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy" ,Locale.ROOT);		    
			df.setLenient(false);
			return df;
		}
	};

	// Used from JSP
	public static boolean domGroupsContainGroupName(ArrayList<LicenseContent> groups, String domGroupName) {
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).getName().equalsIgnoreCase(domGroupName)) {
				return true;
			}
		}
		return false;
	}

	// Used from JSP
	public static boolean domGroupsContainsGroupWithLicense(ArrayList<LicenseContent> groups, String domGroupKey, String presentationTypeKey) {
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).getName().equalsIgnoreCase(domGroupKey)) {
				for (int j = 0; j < groups.get(i).getPresentations().size(); j++) {
					if (groups.get(i).getPresentations().get(j).getKey().equalsIgnoreCase(presentationTypeKey)) {
						return true;
					}
				}
				return false;
			}
		}
		return false;
	}

	public static boolean validateDateFormat(String dateFormat) {
		try {
			Date date = formatter.get().parse(dateFormat);
			String formated = formatter.get().format(date);
			if (!dateFormat.equals(formated)) { // Self check. Due to bug that dateformat accept 2013xxx or 2xxxx as year etc.
				return false;
			}
			return true;
		} catch (Exception e) {
			log.info("Invalid dateformat entered:" + dateFormat);
			return false;
		}

	}

	public static long convertDateFormatToLong(String dateFormat) {
		boolean valid = validateDateFormat(dateFormat);
		if (!valid) {
			throw new IllegalArgumentException("Not valid date:" + dateFormat);
		}
		try {
			return formatter.get().parse(dateFormat).getTime();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not format date:" + dateFormat);
		}
	}

	// For HTML TR CSS style...
	public static String getStyle(int row) {
		if (row % 2 == 0) {
			return "success";
		} else {
			return "error";
		}
	}

}
