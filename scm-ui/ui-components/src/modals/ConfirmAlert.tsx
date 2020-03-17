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
import * as React from "react";
import ReactDOM from "react-dom";
import Modal from "./Modal";
import classNames from "classnames";

type Button = {
  className?: string;
  label: string;
  onClick: () => void | null;
};

type Props = {
  title: string;
  message: string;
  buttons: Button[];
};

class ConfirmAlert extends React.Component<Props> {
  handleClickButton = (button: Button) => {
    if (button.onClick) {
      button.onClick();
    }
    this.close();
  };

  close = () => {
    const container = document.getElementById("modalRoot");
    if (container) {
      ReactDOM.unmountComponentAtNode(container);
    }
  };

  render() {
    const { title, message, buttons } = this.props;

    const body = <>{message}</>;

    const footer = (
      <div className="field is-grouped">
        {buttons.map((button, i) => (
          <p className="control">
            <a className={classNames("button", "is-info", button.className)} key={i} onClick={() => this.handleClickButton(button)}>
              {button.label}
            </a>
          </p>
        ))}
      </div>
    );

    return <Modal title={title} closeFunction={() => this.close()} body={body} active={true} footer={footer} />;
  }
}

export function confirmAlert(properties: Props) {
  const root = document.getElementById("modalRoot");
  if (root) {
    ReactDOM.render(<ConfirmAlert {...properties} />, root);
  }
}

export default ConfirmAlert;
