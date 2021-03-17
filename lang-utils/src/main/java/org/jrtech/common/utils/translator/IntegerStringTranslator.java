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
package org.jrtech.common.utils.translator;


import java.text.NumberFormat;
import java.util.Locale;

public class IntegerStringTranslator implements FormatLocaleAwareStringTranslator<Number> {

    private static final long serialVersionUID = -5581994464574219875L;

    @Override
    public String translate(Number o, Locale locale) {
        if (o == null) {
            return null;
        }

        if (locale == null) {
            // Use system default locale
            return NumberFormat.getIntegerInstance().format(o);
        }

        return NumberFormat.getIntegerInstance(locale).format(o);
    }

}
