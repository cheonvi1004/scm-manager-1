package sonia.scm.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPluginManagerTest {

  @Mock
  private PluginLoader loader;

  @Mock
  private PluginCenter center;

  @Mock
  private PluginInstaller installer;

  @InjectMocks
  private DefaultPluginManager manager;

  @Mock
  private Subject subject;

  @Nested
  class WithAdminPermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void clearThreadContext() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldReturnInstalledPlugins() {
      InstalledPlugin review = createInstalled("scm-review-plugin");
      InstalledPlugin git = createInstalled("scm-git-plugin");

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(review, git));

      List<InstalledPlugin> installed = manager.getInstalled();
      assertThat(installed).containsOnly(review, git);
    }

    @Test
    void shouldReturnReviewPlugin() {
      InstalledPlugin review = createInstalled("scm-review-plugin");
      InstalledPlugin git = createInstalled("scm-git-plugin");

      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(review, git));

      Optional<InstalledPlugin> plugin = manager.getInstalled("scm-review-plugin");
      assertThat(plugin).contains(review);
    }

    @Test
    void shouldReturnEmptyForNonInstalledPlugin() {
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of());

      Optional<InstalledPlugin> plugin = manager.getInstalled("scm-review-plugin");
      assertThat(plugin).isEmpty();
    }

    @Test
    void shouldReturnAvailablePlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");

      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, git));

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available).containsOnly(review, git);
    }

    @Test
    void shouldFilterOutAllInstalled() {
      InstalledPlugin installedGit = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedGit));

      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, git));

      List<AvailablePlugin> available = manager.getAvailable();
      assertThat(available).containsOnly(review);
    }

    @Test
    void shouldReturnAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, git));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).contains(git);
    }

    @Test
    void shouldReturnEmptyForNonExistingAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).isEmpty();
    }

    @Test
    void shouldReturnEmptyForInstalledPlugin() {
      InstalledPlugin installedGit = createInstalled("scm-git-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedGit));

      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(git));

      Optional<AvailablePlugin> available = manager.getAvailable("scm-git-plugin");
      assertThat(available).isEmpty();
    }

    @Test
    void shouldInstallThePlugin() {
      AvailablePlugin git = createAvailable("scm-git-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(git));

      manager.install("scm-git-plugin");

      verify(installer).install(git);
    }

    @Test
    void shouldInstallDependingPlugins() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail));

      manager.install("scm-review-plugin");

      verify(installer).install(mail);
      verify(installer).install(review);
    }

    @Test
    void shouldNotInstallAlreadyInstalledDependencies() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail));

      InstalledPlugin installedMail = createInstalled("scm-mail-plugin");
      when(loader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedMail));

      manager.install("scm-review-plugin");

      verify(installer).install(review);
    }

    @Test
    void shouldRollbackOnFailedInstallation() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(mail.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-notification-plugin"));
      AvailablePlugin notification = createAvailable("scm-notification-plugin");
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail, notification));

      PendingPluginInstallation pendingNotification = mock(PendingPluginInstallation.class);
      doReturn(pendingNotification).when(installer).install(notification);

      PendingPluginInstallation pendingMail = mock(PendingPluginInstallation.class);
      doReturn(pendingMail).when(installer).install(mail);

      doThrow(new PluginChecksumMismatchException("checksum does not match")).when(installer).install(review);

      assertThrows(PluginInstallException.class, () -> manager.install("scm-review-plugin"));

      verify(pendingNotification).cancel();
      verify(pendingMail).cancel();
    }

    @Test
    void shouldInstallNothingIfOneOfTheDependenciesIsNotAvailable() {
      AvailablePlugin review = createAvailable("scm-review-plugin");
      when(review.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-mail-plugin"));
      AvailablePlugin mail = createAvailable("scm-mail-plugin");
      when(mail.getDescriptor().getDependencies()).thenReturn(ImmutableSet.of("scm-notification-plugin"));
      when(center.getAvailable()).thenReturn(ImmutableSet.of(review, mail));

      assertThrows(NotFoundException.class, () -> manager.install("scm-review-plugin"));

      verify(installer, never()).install(any());
    }

  }

  @Nested
  class WithoutReadPermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
      doThrow(AuthorizationException.class).when(subject).checkPermission("plugin:read");
    }

    @AfterEach
    void clearThreadContext() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldThrowAuthorizationExceptionsForReadMethods() {
      assertThrows(AuthorizationException.class, () -> manager.getInstalled());
      assertThrows(AuthorizationException.class, () -> manager.getInstalled("test"));
      assertThrows(AuthorizationException.class, () -> manager.getAvailable());
      assertThrows(AuthorizationException.class, () -> manager.getAvailable("test"));
    }

  }

  @Nested
  class WithoutManagePermissions {

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
      doThrow(AuthorizationException.class).when(subject).checkPermission("plugin:manage");
    }

    @AfterEach
    void clearThreadContext() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldThrowAuthorizationExceptionsForInstallMethod() {
      assertThrows(AuthorizationException.class, () -> manager.install("test"));
    }

  }

  private AvailablePlugin createAvailable(String name) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    return createAvailable(information);
  }

  private InstalledPlugin createInstalled(String name) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    return createInstalled(information);
  }

  private InstalledPlugin createInstalled(PluginInformation information) {
    InstalledPlugin plugin = mock(InstalledPlugin.class, Answers.RETURNS_DEEP_STUBS);
    returnInformation(plugin, information);
    return plugin;
  }

  private AvailablePlugin createAvailable(PluginInformation information) {
    AvailablePlugin plugin = mock(AvailablePlugin.class, Answers.RETURNS_DEEP_STUBS);
    returnInformation(plugin, information);
    return plugin;
  }

  private void returnInformation(Plugin mockedPlugin, PluginInformation information) {
    when(mockedPlugin.getDescriptor().getInformation()).thenReturn(information);
  }

}
