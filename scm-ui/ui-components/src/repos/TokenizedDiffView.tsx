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
import React, { FC } from "react";
import styled from "styled-components";
// @ts-ignore we have no typings for react-diff-view
import { Diff, useTokenizeWorker } from "react-diff-view";
import { File } from "./DiffTypes";

// styling for the diff tokens
// this must be aligned with th style, which is used in the SyntaxHighlighter component
import "highlight.js/styles/arduino-light.css";

const DiffView = styled(Diff)`
  /* align line numbers */
  & .diff-gutter {
    text-align: right;
  }
  /* column sizing */
  > colgroup .diff-gutter-col {
    width: 3.25rem;
  }
  /* prevent following content from moving down */
  > .diff-gutter:empty:hover::after {
    font-size: 0.7rem;
  }
  /* smaller font size for code */
  & .diff-line {
    font-size: 0.75rem;
  }
  /* comment padding for sidebyside view */
  &.split .diff-widget-content .is-indented-line {
    padding-left: 3.25rem;
  }
  /* comment padding for combined view */
  &.unified .diff-widget-content .is-indented-line {
    padding-left: 6.5rem;
  }
`;

// WebWorker which creates tokens for syntax highlighting
const tokenize = new Worker("./Tokenize.worker.ts", { name: "tokenizer", type: "module" });

type Props = {
  file: File;
  viewType: "split" | "unified";
  className?: string;
};

const determineLanguage = (file: File) => {
  if (file.language) {
    return file.language.toLowerCase();
  }
  return "text";
};

const TokenizedDiffView: FC<Props> = ({ file, viewType, className, children }) => {
  const { tokens } = useTokenizeWorker(tokenize, {
    hunks: file.hunks,
    language: determineLanguage(file)
  });

  return (
    <DiffView className={className} viewType={viewType} tokens={tokens} hunks={file.hunks} diffType={file.type}>
      {children}
    </DiffView>
  );
};

export default TokenizedDiffView;
