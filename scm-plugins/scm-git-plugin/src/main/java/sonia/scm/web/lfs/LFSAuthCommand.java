package sonia.scm.web.lfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.CommandInterpreterFactory;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmCommandProtocol;
import sonia.scm.protocolcommand.git.GitRepositoryContextResolver;
import sonia.scm.repository.Repository;
import sonia.scm.security.AccessToken;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private static final String LFS_INFO_URL_PATTERN = "%s/repo/%s/%s.git/info/lfs/";

  private final LfsAccessTokenFactory tokenFactory;
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final ObjectMapper objectMapper;
  private final ScmConfiguration configuration;

  @Inject
  public LFSAuthCommand(LfsAccessTokenFactory tokenFactory, GitRepositoryContextResolver gitRepositoryContextResolver, ScmConfiguration configuration) {
    this.tokenFactory = tokenFactory;
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;

    objectMapper = new ObjectMapper();
    this.configuration = configuration;
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    if (command.startsWith("git-lfs-authenticate")) {
      return Optional.of(new LfsAuthCommandInterpreter(command));
    } else {
      return Optional.empty();
    }
  }

  private class LfsAuthCommandInterpreter implements CommandInterpreter {

    private final String command;

    LfsAuthCommandInterpreter(String command) {
      this.command = command;
    }

    @Override
    public String[] getParsedArgs() {
      // we are interested only in the 'repo' argument, so we discard the rest
      return new String[]{command.split("\\s+")[1]};
    }

    @Override
    public ScmCommandProtocol getProtocolHandler() {
      return (context, repositoryContext) -> {
        ExpiringAction response = createResponseObject(repositoryContext);
        String buffer = serializeResponse(response);
        context.getOutputStream().write(buffer.getBytes(Charsets.UTF_8));
      };
    }

    @Override
    public RepositoryContextResolver getRepositoryContextResolver() {
      return gitRepositoryContextResolver;
    }

    private ExpiringAction createResponseObject(RepositoryContext repositoryContext) {
      Repository repository = repositoryContext.getRepository();

      String url = format(LFS_INFO_URL_PATTERN, configuration.getBaseUrl(), repository.getNamespace(), repository.getName());
      AccessToken accessToken = tokenFactory.createReadAccessToken(repository);

      return new ExpiringAction(url, accessToken);
    }

    private String serializeResponse(ExpiringAction response) throws IOException {
      return objectMapper.writeValueAsString(response);
    }
  }
}
