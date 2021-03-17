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
package org.jrtech.common.xmlutils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class StringArrayNamespaceContext implements NamespaceContext, Serializable {

	private static final long serialVersionUID = -8174755137453096423L;
	
    private String[][] namespaceArray;

	public StringArrayNamespaceContext(String[][] namespaceArray) {
		this.namespaceArray = namespaceArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	@Override
	public String getNamespaceURI(String prefix) {
		for (int i = 0; i < namespaceArray.length; i++) {
			if (prefix.equals(namespaceArray[i][0])) {
				return namespaceArray[i][1];
			}
		}
		return XMLConstants.NULL_NS_URI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	@Override
	public String getPrefix(String namespaceURI) {
		for (int i = 0; i < namespaceArray.length; i++) {
			if (namespaceArray.equals(namespaceArray[i][1]))
				return namespaceArray[i][0];
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	@Override
	public Iterator<String[]> getPrefixes(String namespaceURI) {
		return new ArrayIterator<String[]>(namespaceArray);
	}

	private class ArrayIterator<T> implements Iterator<T>, Serializable, Cloneable {

		private static final long serialVersionUID = -1869785991513310474L;

		private final T[] array;

		private int index;

		private ArrayIterator(T[] array) {
			if (array == null)
				throw new NullPointerException("Empty array assigned for ArrayIterator.");

			this.array = array;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();

			return array[index++];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
