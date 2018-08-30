// @flow
import React from "react";
import { translate } from "react-i18next";
import { Select } from "../../components/forms";

type Props = {
  t: string => string,
  handleTypeChange: string => void,
  type: string,
  loading?: boolean
};

class TypeSelector extends React.Component<Props> {

  render() {
    const { t, type, handleTypeChange, loading } = this.props;
    const types = ["READ", "OWNER", "GROUP"];

    return (
            <Select
              onChange={handleTypeChange}
              value={type ? type : ""}
              options={this.createSelectOptions(types)}
              loading={loading}
            />
    );
  }

  createSelectOptions(types: string[]) {
    return types.map(type => {
      return {
        label: type,
        value: type
      };
    });
  }
}

export default translate("permissions")(TypeSelector);
