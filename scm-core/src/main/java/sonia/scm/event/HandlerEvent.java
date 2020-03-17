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


package sonia.scm.event;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.HandlerEventType;

/**
 * Base class for handler events.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 *
 * @param <T>
 */
public interface HandlerEvent<T>
{

  /**
   * Returns the type of the event,
   *
   *
   * @return event type
   */
  public HandlerEventType getEventType();

  /**
   * Returns the item which has changed.
   *
   *
   * @return changed item
   */
  public T getItem();

  /**
   * Returns old item or null. This method returns always expect for
   * modification events.
   *
   *
   * @return old item or null
   *
   * @since 2.0.0
   */
  public T getOldItem();
}
