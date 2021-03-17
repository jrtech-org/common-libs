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
package org.jrtech.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The class <code>ArrayUtil</code> provides an extension array operations that are
 * not (yet) provided by any open source libraries.
 */
public class ArrayUtil {

    /**
     * Concatenates the two arrays into one of same type of the first.
     * 
     * @param firstItems
     *            first array
     * @param secondItems
     *            second array
     * @return the concatenated array containing the first and the second one.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concatenate(T[] array1, T[] array2) {
        if (array1 == null)
            return array2;
        if (array2 == null)
            return array1;
        T[] array = (T[]) java.lang.reflect.Array.newInstance(array1.getClass().getComponentType(),
                array1.length + array2.length);
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] concatenate(T[] array1, T entry) {
        T[] array = (T[]) java.lang.reflect.Array.newInstance(array1.getClass().getComponentType(), array1.length + 1);
        System.arraycopy(array1, 0, array, 0, array1.length);
        array[array1.length] = entry;
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] subSequence(T[] array, int from, int length) {
        T[] subSequence = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), length);
        System.arraycopy(array, from, subSequence, 0, length);
        return subSequence;
    }

    /**
     * Reads the specified numbers of characters, or at least the maximum
     * numbers that is available.
     * 
     * @param reader
     *            the reader to read from.
     * @param offset
     *            the offset to start from.
     * @param length
     *            the numbers of characters to read.
     * @return the array of characters that was read.
     * @throws IOException
     *             If an I/O exception occurs
     */
    public static char[] read(Reader reader, long offset, int length) throws IOException {
        ArrayList<char[]> bytesArray = new ArrayList<char[]>();
        char[] chars;
        int byteCounter = 0;
        int read, i;
        reader.skip(offset);
        do {
            i = 0;
            int size = Math.min(512, length - byteCounter);
            chars = new char[size];
            do {
                read = reader.read(chars, i, chars.length - i);
                if (read > -1)
                    i += read;
            } while (read > -1 && i < chars.length);
            bytesArray.add(chars);
            byteCounter += i;
        } while (read > -1 && byteCounter < length);
        char[] buffer = new char[byteCounter];
        byteCounter = 0;
        for (int j = 0; j < bytesArray.size() - 1; j++) {
            char[] fragment = bytesArray.get(j);
            System.arraycopy(fragment, 0, buffer, byteCounter, fragment.length);
            byteCounter += fragment.length;
        }
        System.arraycopy(chars, 0, buffer, byteCounter, i);
        return buffer;
    }

    public static byte[] getBytes(InputStream in) throws IOException {
        ArrayList<byte[]> bytesArray = new ArrayList<byte[]>();
        byte[] bytes;
        int byteCounter = 0;
        int read, i;
        do {
            i = 0;
            int available = in.available();
            available = (available < 1 ? 512 : available);
            bytes = new byte[available];
            do {
                read = in.read(bytes, i, bytes.length - i);
                if (read > -1)
                    i += read;
            } while (read > -1 && i < bytes.length);
            bytesArray.add(bytes);
            byteCounter += i;
        } while (read > -1);
        byte[] buffer = new byte[byteCounter];
        byteCounter = 0;
        for (int j = 0; j < bytesArray.size() - 1; j++) {
            byte[] fragment = (byte[]) bytesArray.get(j);
            System.arraycopy(fragment, 0, buffer, byteCounter, fragment.length);
            byteCounter += fragment.length;
        }
        System.arraycopy(bytes, 0, buffer, byteCounter, i);
        return buffer;
    }

    public static boolean hasIntersection(Object[] array1, Object[] array2) {
        for (Object obj1 : array1) {
            for (Object obj2 : array2) {
                if (obj1 != null) {
                    if (obj1.equals(obj2)) {
                        return true;
                    }
                } else if (obj2 == null) {
                    return true;
                }
            }
        }
        return false;
    }

    // public static void main(String[] args) throws Exception {
    // String testData =
    // "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\r\n1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\r\n1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\r\n1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\r\n1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\r\n123456789";
    // StringBufferInputStream in = new StringBufferInputStream(testData) {
    // public synchronized int available() {
    // return 513;
    // }
    // };
    // // byte[] bytes = getBytes(in);
    // char[] read = read(new InputStreamReader(in), 2, 513);
    // System.out.println(new String(read));
    // System.out.println(read.length);
    // }

    /**
     * Convert String e.g. "1, 20, 5" to array of integer int[] {1,20,5}
     * 
     * @param arrayInText
     * @return
     */
    public static int[] convertStringToIntegerArray(String arrayInString) {
        String[] textArray = arrayInString.split(",");
        int[] intArray = new int[textArray.length];
        for (int i = 0; i < textArray.length; i++) {
            try {
                intArray[i] = Integer.parseInt(textArray[i]);
            } catch (Exception e) {
                intArray[i] = 0;
            }
        }
        return intArray;
    }

    /**
     * Convert array of integer e.g. int[] {1,20,5} to String "1, 20, 5"
     * 
     * @param array
     * @return
     */
    public static String convertIntegerArrayToString(int[] array) {
        String arrayInText = "";
        for (int i = 0; i < array.length; i++) {
            arrayInText += array[i] + ",";
        }
        if (arrayInText.endsWith(",")) {
            arrayInText = arrayInText.substring(0, arrayInText.length() - 1);
        }

        return arrayInText;
    }

    public static Integer[] convertObjectCollectionToIntegerArray(Collection<Object> objects) {
        Integer[] inta = new Integer[objects.size()];
        Object[] oa = objects.toArray();
        for (int i = 0; i < objects.size(); i++)
            inta[i] = Integer.parseInt(oa[i].toString());
        return inta;
    }

    public static final String[] valueSplit(String value) {
        if (value == null)
            return null;

        if ("".equals(value))
            return new String[] {};

        List<String> result = new ArrayList<String>();
        String temp = value.trim();

        int startIndex = 0;
        int endIndex = temp.indexOf(',');
        if (endIndex < 0) {
            result.add(temp);
        } else {
            String token = "";
            while (endIndex >= 0 && endIndex < temp.length()) {
                token = "";
                if (startIndex != endIndex) {
                    token = temp.substring((startIndex == 0 ? startIndex : startIndex + 1), endIndex);
                    token = token.trim();
                    startIndex = endIndex;
                }
                result.add(token);
                endIndex = temp.indexOf(',', startIndex + 1);
            }

            // last member
            token = temp.substring((startIndex == 0 ? startIndex : startIndex + 1));
            token = token.trim();
            result.add(token);
        }

        return result.toArray(new String[] {});
    }

    public static int search(byte[] content, byte[] searchPattern, int fromIndex) {
        boolean found = false;
        int i;
        for (i = fromIndex; i < content.length - searchPattern.length; i++) {
            if (content[i] == searchPattern[0]) {
                found = true;
                for (int j = 0; j < searchPattern.length; j++) {
                    if (content[i + j] != searchPattern[j]) {
                        found = false;
                    }
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    public static int search(char[] text, char[] searchString, int fromIndex) {
        boolean found = false;
        int i;
        for (i = fromIndex; i < text.length - searchString.length; i++) {
            if (text[i] == searchString[0]) {
                found = true;
                for (int j = 0; j < searchString.length; j++) {
                    if (text[i + j] != searchString[j]) {
                        found = false;
                    }
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
}
