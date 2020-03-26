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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExitRestartStrategyTest {

  @Mock
  private RestartStrategy.InternalInjectionContext context;

  private ExitRestartStrategy strategy;
  private CapturingExiter exiter;

  @BeforeEach
  void setUpStrategy() {
    strategy = new ExitRestartStrategy();
    exiter = new CapturingExiter();
    strategy.setExiter(exiter);
  }

  @Test
  void shouldTearDownContextAndThenExit() {
    strategy.restart(context);

    verify(context).destroy();
    assertThat(exiter.getExitCode()).isEqualTo(0);
  }

  @Test
  void shouldUseExitCodeFromSystemProperty() {
    System.setProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE, "42");
    try {
      strategy.restart(context);

      verify(context).destroy();
      assertThat(exiter.getExitCode()).isEqualTo(42);
    } finally {
      System.clearProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE);
    }
  }

  @Test
  void shouldThrowExceptionForNonNumericExitCode() {
    System.setProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE, "xyz");
    try {
      assertThrows(RestartNotSupportedException.class, () -> strategy.restart(context));
    } finally {
      System.clearProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE);
    }
  }

  private static class CapturingExiter implements IntConsumer {

    private int exitCode = -1;

    public int getExitCode() {
      return exitCode;
    }

    @Override
    public void accept(int exitCode) {
      this.exitCode = exitCode;
    }
  }
}
