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
import { storiesOf } from "@storybook/react";
import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";

storiesOf("Table|Table", module)
  .add("Default", () => (
    <Table
      data={[
        { firstname: "Tricia", lastname: "McMillan", email: "tricia@hitchhiker.com" },
        { firstname: "Arthur", lastname: "Dent", email: "arthur@hitchhiker.com" }
      ]}
    >
      <Column header={"First Name"}>{(row: any) => <h4>{row.firstname}</h4>}</Column>
      <Column
        header={"Last Name"}
        createComparator={() => {
          return (a: any, b: any) => {
            if (a.lastname > b.lastname) {
              return -1;
            } else if (a.lastname < b.lastname) {
              return 1;
            } else {
              return 0;
            }
          };
        }}
      >
        {(row: any) => <b style={{ color: "red" }}>{row.lastname}</b>}
      </Column>
      <Column header={"E-Mail"}>{(row: any) => <a>{row.email}</a>}</Column>
    </Table>
  ))
  .add("TextColumn", () => (
    <Table
      data={[
        { id: "21", title: "Pommes", desc: "Fried potato sticks" },
        { id: "42", title: "Quarter-Pounder", desc: "Big burger" },
        { id: "-84", title: "Icecream", desc: "Cold dessert" }
      ]}
    >
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="title" />
      <TextColumn header="Description" dataKey="desc" />
    </Table>
  ))
  .add("Empty", () => (
    <Table data={[]} emptyMessage="No data found.">
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="name" />
    </Table>
  ));
