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

package sonia.scm.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Strategy for restarting SCM-Manager. Implementations must either have a default constructor or one taking the
 * class loader for the current context as a single argument.
 */
public abstract class RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(RestartStrategy.class);

  interface InternalInjectionContext extends InjectionContext {
    /**
     * Destroys the injection context.
     */
    void destroy();
  }

  /**
   * Context for Injection in SCM-Manager.
   */
  public interface InjectionContext {
    /**
     * Initialize the injection context.
     */
    void initialize();
  }

  /**
   * Restart SCM-Manager by first calling {@link #prepareRestart(InjectionContext)}, destroying the
   * current context, and finally calling {@link #executeRestart(InjectionContext)}.
   *
   * @param context injection context
   */
  public final void restart(InternalInjectionContext context) {
    prepareRestart(context);
    LOG.warn("destroy injection context");
    context.destroy();
    executeRestart(context);
  }

  /**
   * Prepare the restart of SCM-Manager. Here you can check whether restart is possible and,
   * if necessary, throw a {@link RestartNotSupportedException} to abort the restart.
   *
   * @param context injection context
   */
  protected void prepareRestart(InjectionContext context) {
  }

  /**
   * Actually restart SCM-Manager.
   *
   * @param context injection context
   */
  protected abstract void executeRestart(InjectionContext context);

  /**
   * Returns the configured strategy or empty if restart is not supported by the underlying platform.
   *
   * @param webAppClassLoader root webapp classloader
   * @return configured strategy or empty optional
   */
  static Optional<RestartStrategy> get(ClassLoader webAppClassLoader) {
    return Optional.ofNullable(RestartStrategyFactory.create(webAppClassLoader));
  }

}
