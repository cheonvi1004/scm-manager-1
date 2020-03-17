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
import React from "react";
import { mount, shallow } from "@scm-manager/ui-tests/enzyme-router";
import "@scm-manager/ui-tests/enzyme";
import "@scm-manager/ui-tests/i18n";
import DeletePermissionButton from "./DeletePermissionButton";

import { confirmAlert } from "@scm-manager/ui-components";

jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton
}));

describe("DeletePermissionButton", () => {
  it("should render nothing, if the delete link is missing", () => {
    const permission = {
      _links: {}
    };

    const navLink = shallow(<DeletePermissionButton permission={permission} deletePermission={() => {}} />);
    expect(navLink.text()).toBe("");
  });

  it("should render the delete icon", () => {
    const permission = {
      _links: {
        delete: {
          href: "/permission"
        }
      }
    };

    const deleteIcon = mount(<DeletePermissionButton permission={permission} deletePermission={() => {}} />);
    expect(deleteIcon.html()).not.toBe("");
  });

  it("should open the confirm dialog on button click", () => {
    const permission = {
      _links: {
        delete: {
          href: "/permission"
        }
      }
    };

    const button = mount(<DeletePermissionButton permission={permission} deletePermission={() => {}} />);
    button.find(".fa-trash").simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete permission function with delete url", () => {
    const permission = {
      _links: {
        delete: {
          href: "/permission"
        }
      }
    };

    let calledUrl = null;
    function capture(permission) {
      calledUrl = permission._links.delete.href;
    }

    const button = mount(
      <DeletePermissionButton permission={permission} confirmDialog={false} deletePermission={capture} />
    );
    button.find(".fa-trash").simulate("click");

    expect(calledUrl).toBe("/permission");
  });
});
