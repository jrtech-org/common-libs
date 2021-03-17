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

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VectorNodeList extends Vector<Node> implements NodeList {

    private static final long serialVersionUID = -1074240079831995363L;

    public int getLength() {
        return size();
    }

    public Node item(int index) {
        return (Node) elementAt(index);
    }

}
