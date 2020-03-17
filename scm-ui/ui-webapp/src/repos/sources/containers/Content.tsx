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
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { File, Repository } from "@scm-manager/ui-types";
import { DateFromNow, ErrorNotification, FileSize, Icon } from "@scm-manager/ui-components";
import { getSources } from "../modules/sources";
import FileButtonAddons from "../components/content/FileButtonAddons";
import SourcesView from "./SourcesView";
import HistoryView from "./HistoryView";

type Props = WithTranslation & {
  loading: boolean;
  file: File;
  repository: Repository;
  revision: string;
  path: string;
  breadcrumb: React.ReactNode;
};

type State = {
  collapsed: boolean;
  showHistory: boolean;
  errorFromExtension?: Error;
};

const Header = styled.div`
  border-bottom: solid 1px #dbdbdb;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
`;

const VCenteredChild = styled.div`
  align-items: center;
`;

const RightMarginIcon = styled(Icon)`
  margin-right: 0.5em;
`;

const RightMarginFileButtonAddons = styled(FileButtonAddons)`
  margin-right: 0.5em;
`;

const BorderBottom = styled.div`
  border-bottom: solid 1px #dbdbdb;
`;

const LighterGreyBackgroundPanelBlock = styled.div`
  background-color: #fbfbfb;
`;

const LighterGreyBackgroundTable = styled.table`
  background-color: #fbfbfb;
`;

class Content extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      collapsed: true,
      showHistory: false
    };
  }

  toggleCollapse = () => {
    this.setState(prevState => ({
      collapsed: !prevState.collapsed
    }));
  };

  setShowHistoryState(showHistory: boolean) {
    this.setState({
      ...this.state,
      showHistory
    });
  }

  handleExtensionError = (error: Error) => {
    this.setState({
      errorFromExtension: error
    });
  };

  showHeader() {
    const { repository, file, revision } = this.props;
    const { showHistory, collapsed } = this.state;
    const icon = collapsed ? "angle-right" : "angle-down";

    const selector = file._links.history ? (
      <RightMarginFileButtonAddons
        file={file}
        historyIsSelected={showHistory}
        showHistory={(changeShowHistory: boolean) => this.setShowHistoryState(changeShowHistory)}
      />
    ) : null;

    return (
      <span className="has-cursor-pointer">
        <VCenteredChild className={classNames("media", "is-flex")}>
          <div className="media-content" onClick={this.toggleCollapse}>
            <RightMarginIcon name={icon} color="inherit" />
            <span className="is-word-break">{file.name}</span>
          </div>
          <div className="buttons is-grouped">
            {selector}
            <ExtensionPoint
              name="repos.sources.content.actionbar"
              props={{
                repository,
                file,
                revision,
                handleExtensionError: this.handleExtensionError
              }}
              renderAll={true}
            />
          </div>
        </VCenteredChild>
      </span>
    );
  }

  showMoreInformation() {
    const collapsed = this.state.collapsed;
    const { file, revision, t, repository } = this.props;
    const date = <DateFromNow date={file.commitDate} />;
    const description = file.description ? (
      <p>
        {file.description.split("\n").map((item, key) => {
          return (
            <span key={key}>
              {item}
              <br />
            </span>
          );
        })}
      </p>
    ) : null;
    const fileSize = file.directory ? "" : <FileSize bytes={file?.length ? file.length : 0} />;
    if (!collapsed) {
      return (
        <>
          <LighterGreyBackgroundPanelBlock className="panel-block">
            <LighterGreyBackgroundTable className="table">
              <tbody>
                <tr>
                  <td>{t("sources.content.path")}</td>
                  <td className="is-word-break">{file.path}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.branch")}</td>
                  <td className="is-word-break">{decodeURIComponent(revision)}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.size")}</td>
                  <td>{fileSize}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.commitDate")}</td>
                  <td>{date}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.description")}</td>
                  <td className="is-word-break">{description}</td>
                </tr>
                <ExtensionPoint
                  name="repos.content.metadata"
                  renderAll={true}
                  props={{
                    file,
                    repository,
                    revision
                  }}
                />
              </tbody>
            </LighterGreyBackgroundTable>
          </LighterGreyBackgroundPanelBlock>
          <BorderBottom />
        </>
      );
    }
    return null;
  }

  render() {
    const { file, revision, repository, path, breadcrumb } = this.props;
    const { showHistory, errorFromExtension } = this.state;

    const header = this.showHeader();
    const content =
      showHistory && file._links.history ? (
        <HistoryView file={file} repository={repository} />
      ) : (
        <SourcesView revision={revision} file={file} repository={repository} path={path} />
      );
    const moreInformation = this.showMoreInformation();

    return (
      <div>
        <div className="panel">
          {breadcrumb}
          <Header>{header}</Header>
          {moreInformation}
          {content}
        </div>
        {errorFromExtension && <ErrorNotification error={errorFromExtension} />}
      </div>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  const file = getSources(state, repository, revision, path);

  return {
    file
  };
};

export default connect(mapStateToProps)(withTranslation("repos")(Content));
