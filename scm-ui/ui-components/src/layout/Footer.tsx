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
import { Me, Links } from "@scm-manager/ui-types";
import { useBinder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { AvatarImage } from "../avatar";
import NavLink from "../navigation/NavLink";
import FooterSection from "./FooterSection";
import styled from "styled-components";
import { EXTENSION_POINT } from "../avatar/Avatar";
import ExternalLink from "../navigation/ExternalLink";
import { useTranslation } from "react-i18next";

type Props = {
  me?: Me;
  version: string;
  links: Links;
};

type TitleWithIconsProps = {
  title: string;
  icon: string;
};

const TitleWithIcon: FC<TitleWithIconsProps> = ({ icon, title }) => (
  <>
    <i className={`fas fa-${icon} fa-fw`} /> {title}
  </>
);

type TitleWithAvatarProps = {
  me: Me;
};

const VCenteredAvatar = styled(AvatarImage)`
  vertical-align: middle;
`;

const AvatarContainer = styled.span`
  float: left;
  margin-right: 0.3em;
  padding-top: 0.2em;
  width: 1em;
  height: 1em;
`;

const TitleWithAvatar: FC<TitleWithAvatarProps> = ({ me }) => (
  <>
    <AvatarContainer className="image is-rounded">
      <VCenteredAvatar person={me} representation="rounded" />
    </AvatarContainer>
    {me.displayName}
  </>
);

const Footer: FC<Props> = ({ me, version, links }) => {
  const [t] = useTranslation("commons");
  const binder = useBinder();
  if (!me) {
    return null;
  }

  const extensionProps = { me, url: "/me", links };
  let meSectionTile;
  if (binder.hasExtension(EXTENSION_POINT)) {
    meSectionTile = <TitleWithAvatar me={me} />;
  } else {
    meSectionTile = <TitleWithIcon title={me.displayName} icon="user-circle" />;
  }

  return (
    <footer className="footer">
      <section className="section container">
        <div className="columns is-size-7">
          <FooterSection title={meSectionTile}>
            <NavLink to="/me" label={t("footer.user.profile")} />
            <NavLink to="/me/settings/password" label={t("profile.changePasswordNavLink")} />
            <ExtensionPoint name="profile.setting" props={extensionProps} renderAll={true} />
          </FooterSection>
          <FooterSection title={<TitleWithIcon title={t("footer.information.title")} icon="info-circle" />}>
            <ExternalLink to="https://www.scm-manager.org/" label={`SCM-Manager ${version}`} />
            <ExtensionPoint name="footer.information" props={extensionProps} renderAll={true} />
          </FooterSection>
          <FooterSection title={<TitleWithIcon title={t("footer.support.title")} icon="life-ring" />}>
            <ExternalLink to="https://www.scm-manager.org/support/" label={t("footer.support.community")} />
            <ExternalLink to="https://cloudogu.com/en/scm-manager-enterprise/" label={t("footer.support.enterprise")} />
            <ExtensionPoint name="footer.support" props={extensionProps} renderAll={true} />
          </FooterSection>
        </div>
      </section>
    </footer>
  );
};

export default Footer;
