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
import React, { MouseEvent } from "react";
import styled from "styled-components";
import Level from "../layout/Level";
import InputField from "./InputField";
import AddButton from "../buttons/AddButton";

type Props = {
  addEntry: (p: string) => void;
  disabled: boolean;
  buttonLabel: string;
  fieldLabel: string;
  errorMessage: string;
  helpText?: string;
  validateEntry?: (p: string) => boolean;
};

type State = {
  entryToAdd: string;
};

const StyledLevel = styled(Level)`
  align-items: stretch;
  margin-bottom: 1rem !important; // same margin as field
`;

const StyledInputField = styled(InputField)`
  width: 100%;
  margin-right: 1.5rem;
`;

const StyledField = styled.div.attrs(props => ({
  className: "field"
}))`
  align-self: flex-end;
`;

class AddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      entryToAdd: ""
    };
  }

  isValid = () => {
    const { validateEntry } = this.props;
    if (!this.state.entryToAdd || this.state.entryToAdd === "" || !validateEntry) {
      return true;
    } else {
      return validateEntry(this.state.entryToAdd);
    }
  };

  render() {
    const { disabled, buttonLabel, fieldLabel, errorMessage, helpText } = this.props;
    return (
      <StyledLevel
        children={
          <StyledInputField
            label={fieldLabel}
            errorMessage={errorMessage}
            onChange={this.handleAddEntryChange}
            validationError={!this.isValid()}
            value={this.state.entryToAdd}
            onReturnPressed={this.appendEntry}
            disabled={disabled}
            helpText={helpText}
          />
        }
        right={
          <StyledField>
            <AddButton
              label={buttonLabel}
              action={this.addButtonClicked}
              disabled={disabled || this.state.entryToAdd === "" || !this.isValid()}
            />
          </StyledField>
        }
      />
    );
  }

  addButtonClicked = (event: MouseEvent) => {
    event.preventDefault();
    this.appendEntry();
  };

  appendEntry = () => {
    const { entryToAdd } = this.state;
    this.props.addEntry(entryToAdd);
    this.setState({
      ...this.state,
      entryToAdd: ""
    });
  };

  handleAddEntryChange = (entryname: string) => {
    this.setState({
      ...this.state,
      entryToAdd: entryname
    });
  };
}

export default AddEntryToTableField;
