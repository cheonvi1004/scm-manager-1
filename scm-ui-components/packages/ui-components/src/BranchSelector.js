// @flow

import React from "react";
import type { Branch, Repository } from "@scm-manager/ui-types";
import injectSheet from "react-jss";
import classNames from "classnames";
import DropDown from "./forms/DropDown";
import { apiClient } from "./apiclient";

const styles = {
  zeroflex: {
    flexGrow: 0
  },
  minWidthOfLabel: {
    minWidth: "4.5rem"
  },
  wrapper: {
    padding: "1rem 1.5rem 0.25rem 1.5rem",
    border: "1px solid #eee",
    borderRadius: "5px 5px 0 0"
  }
};

type Props = {
  branches: Branch[], // TODO: Use generics?
  selected: (branch?: Branch) => void,
  selectedBranch?: string,
  label: string,
  repository: Repository,

  // context props
  classes: Object
};

type State = { selectedBranch?: Branch };

class BranchSelector extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const selectedBranch = this.props.branches.find(
      branch => branch.name === this.props.selectedBranch
    );

    if (
      !selectedBranch &&
      this.props.repository &&
      this.props.repository._links.configuration
    ) {
      apiClient
        .get(this.props.repository._links.configuration.href)
        .then(response => response.json())
        .then(payload => payload.defaultBranch)
        .then(defaultBranch => {
          const selectedBranch = this.props.branches.find(
            branch => branch.name === defaultBranch
          );
          this.setState({ selectedBranch });
        })
        .catch(error => this.setState({ selectedBranch: undefined }));
    } else {
      this.setState({ selectedBranch });
    }
  }

  render() {
    const { branches, classes, label } = this.props;

    if (branches) {
      return (
        <div
          className={classNames(
            "has-background-light field",
            "is-horizontal",
            classes.wrapper
          )}
        >
          <div
            className={classNames(
              "field-label",
              "is-normal",
              classes.zeroflex,
              classes.minWidthOfLabel
            )}
          >
            <label className="label">{label}</label>
          </div>
          <div className="field-body">
            <div className="field is-narrow">
              <div className="control">
                <DropDown
                  className="is-fullwidth"
                  options={branches.map(b => b.name)}
                  optionSelected={this.branchSelected}
                  preselectedOption={
                    this.state.selectedBranch
                      ? this.state.selectedBranch.name
                      : ""
                  }
                />
              </div>
            </div>
          </div>
        </div>
      );
    } else {
      return null;
    }
  }

  branchSelected = (branchName: string) => {
    const { branches, selected } = this.props;
    if (!branchName) {
      this.setState({ selectedBranch: undefined });

      selected(undefined);
      return;
    }
    const branch = branches.find(b => b.name === branchName);

    selected(branch);
    this.setState({ selectedBranch: branch });
  };
}

export default injectSheet(styles)(BranchSelector);
