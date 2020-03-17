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
import { connect } from "react-redux";
import { compose } from "redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { User } from "@scm-manager/ui-types";
import { confirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { deleteUser, getDeleteUserFailure, isDeleteUserPending } from "../modules/users";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  user: User;
  confirmDialog?: boolean;
  deleteUser: (user: User, callback?: () => void) => void;

  // context props
  history: History;
};

class DeleteUser extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  userDeleted = () => {
    this.props.history.push("/users/");
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.user, this.userDeleted);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("deleteUser.confirmAlert.title"),
      message: t("deleteUser.confirmAlert.message"),
      buttons: [
        {
          className: "is-outlined",
          label: t("deleteUser.confirmAlert.submit"),
          onClick: () => this.deleteUser()
        },
        {
          label: t("deleteUser.confirmAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    const { loading, error, confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteUser;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <hr />
        <ErrorNotification error={error} />
        <Level right={<DeleteButton label={t("deleteUser.button")} action={action} loading={loading} />} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isDeleteUserPending(state, ownProps.user.name);
  const error = getDeleteUserFailure(state, ownProps.user.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    deleteUser: (user: User, callback?: () => void) => {
      dispatch(deleteUser(user, callback));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter, withTranslation("users"))(DeleteUser);
