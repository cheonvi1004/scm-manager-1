/**
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
package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.user.UserDAO;

import javax.ws.rs.NotAuthorizedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymousRealmTest {

  @Mock
  private DAORealmHelperFactory realmHelperFactory;

  @Mock
  private DAORealmHelper realmHelper;

  @Mock
  private DAORealmHelper.AuthenticationInfoBuilder builder;

  @Mock
  private UserDAO userDAO;

  @InjectMocks
  private AnonymousRealm realm;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @BeforeEach
  void prepareObjectUnderTest() {
    when(realmHelperFactory.create(AnonymousRealm.REALM)).thenReturn(realmHelper);
    realm = new AnonymousRealm(realmHelperFactory, userDAO);
  }

  @Test
  void shouldDoGetAuthentication() {
    when(realmHelper.authenticationInfoBuilder(SCMContext.USER_ANONYMOUS)).thenReturn(builder);
    when(builder.build()).thenReturn(authenticationInfo);
    when(userDAO.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);

    AuthenticationInfo result = realm.doGetAuthenticationInfo(new AnonymousToken());
    assertThat(result).isSameAs(authenticationInfo);
  }

  @Test
  void shouldThrowNotAuthorizedExceptionIfAnonymousUserNotExists() {
    when(userDAO.contains(SCMContext.USER_ANONYMOUS)).thenReturn(false);
    assertThrows(NotAuthorizedException.class, () -> realm.doGetAuthenticationInfo(new AnonymousToken()));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForWrongTypeOfToken() {
    when(userDAO.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> realm.doGetAuthenticationInfo(new UsernamePasswordToken()));
  }
}
