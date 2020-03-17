/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package sonia.scm.repository.xml;

import sonia.scm.repository.RepositoryRole;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlRepositoryRoleMapAdapter
        extends XmlAdapter<XmlRepositoryRoleList, Map<String, RepositoryRole>> {

  @Override
  public XmlRepositoryRoleList marshal(Map<String, RepositoryRole> roleMap) {
    return new XmlRepositoryRoleList(roleMap);
  }

  @Override
  public Map<String, RepositoryRole> unmarshal(XmlRepositoryRoleList roles) {
    Map<String, RepositoryRole> roleMap = new LinkedHashMap<>();

    for (RepositoryRole role : roles) {
      roleMap.put(role.getName(), role);
    }

    return roleMap;
  }
}
