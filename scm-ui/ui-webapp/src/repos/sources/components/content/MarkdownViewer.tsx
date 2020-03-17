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
import React, { FC, useEffect, useState } from "react";
import { getContent } from "./SourcecodeViewer";
import { Link, File } from "@scm-manager/ui-types";
import { Loading, ErrorNotification, MarkdownView } from "@scm-manager/ui-components";
import styled from "styled-components";

type Props = {
  file: File;
};

const MarkdownContent = styled.div`
  padding: 0.5rem;
`;

const MarkdownViewer: FC<Props> = ({ file }) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [content, setContent] = useState("");

  useEffect(() => {
    getContent((file._links.self as Link).href)
      .then(content => {
        setLoading(false);
        setContent(content);
      })
      .catch(error => {
        setLoading(false);
        setError(error);
      });
  }, [file]);

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <MarkdownContent>
      <MarkdownView content={content} />
    </MarkdownContent>
  );
};

export default MarkdownViewer;
