/*
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
import React, { FC, ReactElement, useContext, useEffect } from "react";
import { Link, useRouteMatch } from "react-router-dom";
import classNames from "classnames";

type Props = {
  to: string;
  icon?: string;
  label: string;
  activeOnlyWhenExact?: boolean;
  activeWhenMatch?: (route: any) => boolean;
  children?: ReactElement[];
  collapsed?: boolean;
  title?: string;
};

const SubNavigation: FC<Props> = ({ to, activeOnlyWhenExact, icon, collapsed, title, label, children }) => {
  const parents = to.split("/");
  parents.splice(-1, 1);
  const parent = parents.join("/");

  const match = useRouteMatch({
    path: parent,
    exact: activeOnlyWhenExact
  });

  let defaultIcon = "fas fa-cog";
  if (icon) {
    defaultIcon = icon;
  }

  let childrenList = null;
  if (match && !collapsed) {
    childrenList = <ul className="sub-menu">{children}</ul>;
  }

  return (
    <li title={collapsed ? title : undefined}>
      <Link className={classNames(match != null ? "is-active" : "", collapsed ? "has-text-centered" : "")} to={to}>
        <i className={classNames(defaultIcon, "fa-fw")} /> {collapsed ? "" : label}
      </Link>
      {childrenList}
    </li>
  );
};

export default SubNavigation;
