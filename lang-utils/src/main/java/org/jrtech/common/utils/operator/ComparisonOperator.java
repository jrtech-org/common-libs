/*
 * Copyright (c) 2016-2026 Jumin Rubin
 * LinkedIn: https://www.linkedin.com/in/juminrubin/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jrtech.common.utils.operator;


import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComparisonOperator implements Serializable {

	private static final long serialVersionUID = 7613128637268720207L;

	private final String value;

	// The list of the constant objects
	private static final List<ComparisonOperator> INSTANCES = new ArrayList<ComparisonOperator>();

	public static final ComparisonOperator EQ = new ComparisonOperator("=");
	public static final ComparisonOperator NE = new ComparisonOperator("!=");
	public static final ComparisonOperator GT = new ComparisonOperator(">");
	public static final ComparisonOperator GE = new ComparisonOperator(">=");
	public static final ComparisonOperator LT = new ComparisonOperator("<");
	public static final ComparisonOperator LE = new ComparisonOperator("<=");

	private ComparisonOperator(String value) {
		this.value = value;
		INSTANCES.add(this);
	}

	public String toString() {
		return value;
	}

	public static ComparisonOperator getInstance(String value) {
		Iterator<ComparisonOperator> i = INSTANCES.iterator();
		while (i.hasNext()) {
			ComparisonOperator instance = (ComparisonOperator) i.next();
			if (instance.value.equals(value))
				return instance;
		}
		return null;
	}

	public static ComparisonOperator getReverseInstance(ComparisonOperator op) {
		ComparisonOperator instance = op;
		if (op == GT)
			instance = LE;
		else if (op == LE)
			instance = GT;
		else if (op == GE)
			instance = LT;
		else if (op == LT)
			instance = GE;
		else if (op == EQ)
			instance = NE;
		else if (op == NE)
			instance = EQ;
		return instance;
	}

	public static ComparisonOperator getReverseInstance(String value) {
		Iterator<ComparisonOperator> i = INSTANCES.iterator();
		while (i.hasNext()) {
			ComparisonOperator instance = (ComparisonOperator) i.next();
			if (instance.value.equals(value))
				return getReverseInstance(instance);
		}
		return null;
	}

	public static ComparisonOperator[] getInstances() {
		return (ComparisonOperator[]) (INSTANCES.toArray(new ComparisonOperator[INSTANCES.size()]));
	}

	public String getValue() {
		return value;
	}

	public static String[] getValues() {
		List<String> values = new ArrayList<String>();
		Iterator<ComparisonOperator> i = INSTANCES.iterator();
		while (i.hasNext()) {
			ComparisonOperator instance = (ComparisonOperator) i.next();
			values.add(instance.value);
		}
		return values.toArray(new String[values.size()]);
	}

	private Object readResolve() throws ObjectStreamException {
		return getInstance(value);
	}

}
