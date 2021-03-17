package org.jrtech.common.utils.translator;


import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleTimestampTranslator implements StringTranslator<Date> {

    private static final long serialVersionUID = -8996442007401993597L;
    
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@Override
    public String translate(Date o) {
		if (o == null) return null;
		
	    return FORMATTER.format(o);
    }

}
