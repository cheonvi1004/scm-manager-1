import React, { useEffect, useState } from "react";
import { storiesOf } from "@storybook/react";
import Diff from "./Diff";
// @ts-ignore
import parser from "gitdiff-parser";
import simpleDiff from "../__resources__/Diff.simple";
import hunksDiff from "../__resources__/Diff.hunks";
import Button from "../buttons/Button";
import { DiffEventContext } from "./DiffTypes";
import Toast from "../toast/Toast";

const diffFiles = parser.parse(simpleDiff);

storiesOf("Diff", module)
  .add("Default", () => <Diff diff={diffFiles} />)
  .add("Side-By-Side", () => <Diff diff={diffFiles} sideBySide={true} />)
  .add("Collapsed", () => <Diff diff={diffFiles} defaultCollapse={true} />)
  .add("File Controls", () => <Diff diff={diffFiles} fileControlFactory={() => <Button>Custom Control</Button>} />)
  .add("File Annotation", () => (
    <Diff
      diff={diffFiles}
      fileAnnotationFactory={file => [<p key={file.newPath}>Custom File annotation for {file.newPath}</p>]}
    />
  ))
  .add("Line Annotation", () => (
    <Diff
      diff={diffFiles}
      annotationFactory={ctx => {
        return {
          N2: <p key="N2">Line Annotation</p>
        };
      }}
    />
  ))
  .add("OnClick", () => {
    const OnClickDemo = () => {
      const [changeId, setChangeId] = useState();
      useEffect(() => {
        const interval = setInterval(() => setChangeId(undefined), 2000);
        return () => clearInterval(interval);
      });
      const onClick = (context: DiffEventContext) => setChangeId(context.changeId);
      return (
        <>
          {changeId && <Toast type="info" title={"Change " + changeId} />}
          <Diff diff={diffFiles} onClick={onClick} />
        </>
      );
    };
    return <OnClickDemo />;
  })
  .add("Hunks", () => {
    const hunkDiffFiles = parser.parse(hunksDiff);
    return <Diff diff={hunkDiffFiles} />;
  });