package sonia.scm.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CronTaskTest {

  @Mock
  private CronExpression expression;

  @Mock
  private Runnable runnable;

  @Mock
  private Future<?> future;

  @Test
  void shouldReturnTrue() {
    when(expression.calculateNextRun()).thenReturn(Optional.of(ZonedDateTime.now()));

    CronTask task = task();

    assertThat(task.hasNextRun()).isTrue();
  }

  @Test
  void shouldReturnFalse() {
    when(expression.calculateNextRun()).thenReturn(Optional.empty());

    CronTask task = task();

    assertThat(task.hasNextRun()).isFalse();
  }

  private CronTask task() {
    return new CronTask("one", expression, runnable);
  }

  @Test
  void shouldCancelWithoutNextRun() {
    ZonedDateTime time = ZonedDateTime.now();
    when(expression.calculateNextRun()).thenReturn(Optional.of(time), Optional.empty());
    when(expression.shouldRun(time)).thenReturn(true);

    CronTask task = task();
    task.setFuture(future);
    task.run();

    verify(runnable).run();
    verify(future).cancel(false);
  }

  @Test
  void shouldNotRunAfterCancelHasBeenCalledIfRunIsCalledAgain() {
    ZonedDateTime time = ZonedDateTime.now();
    when(expression.calculateNextRun()).thenReturn(Optional.of(time), Optional.empty());
    when(expression.shouldRun(time)).thenReturn(true);

    CronTask task = task();
    task.setFuture(future);

    task.run();
    task.run();

    verify(future).cancel(false);
    verify(runnable).run();
  }

  @Test
  void shouldNotRun() {
    task().run();

    verify(runnable, never()).run();
  }
}
