/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class HttpUtil
{

  /** Field description */
  public static final String AUTHENTICATION_REALM = "SONIA :: SCM Manager";

  /** Field description */
  public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

  /**
   * Default http port
   * @since 1.5
   */
  public static final int PORT_HTTP = 80;

  /**
   * Default https port
   * @since 1.5
   */
  public static final int PORT_HTTPS = 443;

  /**
   * Default http scheme
   * @since 1.5
   */
  public static final String SCHEME_HTTP = "http";

  /**
   * Default https scheme
   * @since 1.5
   */
  public static final String SCHEME_HTTPS = "https";

  /**
   * Url folder separator
   * @since 1.5
   */
  public static final char SEPARATOR_FOLDER = '/';

  /**
   * Url port separator
   * @since 1.5
   */
  public static final char SEPARATOR_PORT = ':';

  /** Field description */
  public static final String STATUS_UNAUTHORIZED_MESSAGE =
    "Authorization Required";

  /** the logger for HttpUtil */
  private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   *
   * @throws IOException
   */
  public static void sendUnauthorized(HttpServletResponse response)
          throws IOException
  {
    response.setHeader(
        HEADER_WWW_AUTHENTICATE,
        "Basic realm=\"".concat(AUTHENTICATION_REALM).concat("\""));
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                       STATUS_UNAUTHORIZED_MESSAGE);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   * @param path
   *
   * @return
   * @since 1.5
   */
  public static String getCompleteUrl(ScmConfiguration configuration,
          String path)
  {
    String url = configuration.getBaseUrl();

    if (url.endsWith("/") && path.startsWith("/"))
    {
      url = url.substring(0, url.length());
    }

    return url.concat(path);
  }

  /**
   * Returns the port of the url parameter.
   *
   * @param url
   * @return port of url
   */
  public static int getPortFromUrl(String url)
  {
    AssertUtil.assertIsNotEmpty(url);

    int port = PORT_HTTP;
    int index = url.indexOf(SEPARATOR_PORT);

    if (index > 0)
    {
      String portString = url.substring(index);
      int slIndex = portString.indexOf(SEPARATOR_FOLDER);

      if (slIndex > 0)
      {
        portString = portString.substring(0, slIndex);
      }

      try
      {
        port = Integer.parseInt(portString);
      }
      catch (NumberFormatException ex)
      {
        logger.error("could not parse port part of url", ex);
      }
    }
    else if (url.startsWith(SCHEME_HTTPS))
    {
      port = PORT_HTTPS;
    }

    return port;
  }

  /**
   * Returns the server port
   *
   *
   * @param configuration
   * @param request
   *
   * @return the server port
   */
  public static int getServerPort(ScmConfiguration configuration,
                                  HttpServletRequest request)
  {
    int port = PORT_HTTP;
    String baseUrl = configuration.getBaseUrl();

    if (Util.isNotEmpty(baseUrl))
    {
      port = getPortFromUrl(baseUrl);
    }
    else
    {
      port = request.getServerPort();
    }

    return port;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  public static String getStrippedURI(HttpServletRequest request)
  {
    return getStrippedURI(request, request.getRequestURI());
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param uri
   *
   * @return
   */
  public static String getStrippedURI(HttpServletRequest request, String uri)
  {
    return uri.substring(request.getContextPath().length());
  }
}
