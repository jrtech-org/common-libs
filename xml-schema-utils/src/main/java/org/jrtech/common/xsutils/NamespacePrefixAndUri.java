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
package org.jrtech.common.xsutils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NamespacePrefixAndUri implements Serializable {

    private static final long serialVersionUID = 2758474910314897080L;

    private final String prefix;

    private final String uri;

    public NamespacePrefixAndUri(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "'" + prefix + "'='" + uri + "'";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NamespacePrefixAndUri other = (NamespacePrefixAndUri) obj;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    public static String[] getPrefixes(NamespacePrefixAndUri[] namespaceArray) {
        List<String> prefixes = new ArrayList<String>();

        for (int i = 0; i < namespaceArray.length; i++) {
            String prefix = namespaceArray[i].getPrefix();
            if (prefixes.contains(prefix))
                continue;

            prefixes.add(prefix);
        }

        return prefixes.toArray(new String[] {});
    }

    public static String[] getUris(NamespacePrefixAndUri[] namespaceArray) {
        List<String> uris = new ArrayList<String>();

        for (int i = 0; i < namespaceArray.length; i++) {
            String uri = namespaceArray[i].getUri();
            if (uris.contains(uri))
                continue;

            uris.add(uri);
        }

        return uris.toArray(new String[] {});
    }

    public static String[][] toStringArray(NamespacePrefixAndUri[] namespaceArray) {
        String[][] stringArray = new String[namespaceArray.length][2];

        for (int i = 0; i < namespaceArray.length; i++) {
            stringArray[i][0] = namespaceArray[i].getPrefix();
            stringArray[i][1] = namespaceArray[i].getUri();
        }

        return stringArray;
    }

    public static String[][] toStringArray(Collection<NamespacePrefixAndUri> namespaceCollection) {
        if (namespaceCollection == null)
            return null;

        String[][] stringArray = new String[namespaceCollection.size()][2];

        int i = 0;
        for (NamespacePrefixAndUri item : namespaceCollection) {
            stringArray[i][0] = item.getPrefix();
            stringArray[i][1] = item.getUri();
            i++;
        }

        return stringArray;
    }
}
