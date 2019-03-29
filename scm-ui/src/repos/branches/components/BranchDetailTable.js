//@flow
import React from "react";
import type { Repository, Branch } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import BranchButtonGroup from "./BranchButtonGroup";

type Props = {
  repository: Repository,
  branch: Branch,
  // context props
  t: string => string
};

class BranchDetailTable extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;
    return (
      <table className="table">
        <tbody>
          <tr>
            <td className="has-text-weight-semibold">{t("branch.name")}</td>
            <td>branch.name</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">
              {t("branch.repository")}
            </td>
            <td>{repository.name}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">
              {t("branch.actions")}
            </td>
            <td><BranchButtonGroup repository={repository} branch={branch} /></td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default translate("repos")(BranchDetailTable);
