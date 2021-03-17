package org.jrtech.common.utils.translator;


import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public class StringCollectionStringTranslator implements StringTranslator<Collection<String>> {

	private static final long serialVersionUID = 7396731821088529725L;

    @Override
	public String translate(Collection<String> o) {
		if (o == null) return null;
		
		return StringUtils.join(o, ";");
	}

}
