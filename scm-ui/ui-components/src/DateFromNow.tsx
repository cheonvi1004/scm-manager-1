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
import { withTranslation, WithTranslation } from "react-i18next";
import { formatDistance, format, parseISO, Locale } from "date-fns";
import { enUS, de, es } from "date-fns/locale";
import styled from "styled-components";

type LocaleMap = {
  [key: string]: Locale;
};

type DateInput = Date | string;

export const supportedLocales: LocaleMap = {
  enUS,
  en: enUS,
  de,
  es
};

type Props = WithTranslation & {
  date?: DateInput;
  timeZone?: string;

  /**
   * baseDate is the date from which the distance is calculated,
   * default is the current time (new Date()). This property
   * is required to keep snapshots tests green over the time on
   * ci server.
   */
  baseDate?: DateInput;
};

type Options = {
  addSuffix: boolean;
  locale: Locale;
  timeZone?: string;
};

const DateElement = styled.time`
  border-bottom: 1px dotted rgba(219, 219, 219);
  cursor: help;
`;

export const chooseLocale = (language: string, languages?: string[]) => {
  for (const lng of languages || []) {
    const locale = supportedLocales[lng];
    if (locale) {
      return locale;
    }
  }

  const locale = supportedLocales[language];
  if (locale) {
    return locale;
  }

  return enUS;
};

class DateFromNow extends React.Component<Props> {
  getLocale = (): Locale => {
    const { i18n } = this.props;
    return chooseLocale(i18n.language, i18n.languages);
  };

  createOptions = () => {
    const { timeZone } = this.props;
    const options: Options = {
      addSuffix: true,
      locale: this.getLocale()
    };
    if (timeZone) {
      options.timeZone = timeZone;
    }
    return options;
  };

  toDate = (value: DateInput): Date => {
    if (value instanceof Date) {
      return value;
    }
    return parseISO(value);
  };

  getBaseDate = () => {
    const { baseDate } = this.props;
    if (baseDate) {
      return this.toDate(baseDate);
    }
    return new Date();
  };

  render() {
    const { date } = this.props;
    if (date) {
      const isoDate = this.toDate(date);
      const options = this.createOptions();
      const distance = formatDistance(isoDate, this.getBaseDate(), options);
      const formatted = format(isoDate, "yyyy-MM-dd HH:mm:ss", options);
      return <DateElement title={formatted}>{distance}</DateElement>;
    }
    return null;
  }
}

export default withTranslation()(DateFromNow);
