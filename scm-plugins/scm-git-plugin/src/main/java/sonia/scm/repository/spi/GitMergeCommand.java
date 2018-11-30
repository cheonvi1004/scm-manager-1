package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.user.User;

import java.io.IOException;
import java.text.MessageFormat;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeCommand.class);

  private static final String MERGE_COMMIT_MESSAGE_TEMPLATE = String.join("\n",
    "Merge of branch {0} into {1}",
    "",
    "Automatic merge by SCM-Manager.");

  private final GitWorkdirFactory workdirFactory;

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    try (WorkingCopy workingCopy = workdirFactory.createWorkingCopy(context)) {
      Repository repository = workingCopy.get();
      logger.debug("cloned repository to folder {}", repository.getWorkTree());
      return new MergeWorker(repository, request).merge();
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
    }
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try {
      Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(merger.merge(repository.resolve(request.getBranchToMerge()), repository.resolve(request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
    }
  }

  private class MergeWorker {

    private final String target;
    private final String toMerge;
    private final Person author;
    private final Git clone;
    private final String messageTemplate;

    private MergeWorker(Repository clone, MergeCommandRequest request) {
      this.target = request.getTargetBranch();
      this.toMerge = request.getBranchToMerge();
      this.author = request.getAuthor();
      this.messageTemplate = request.getMessageTemplate();
      this.clone = new Git(clone);
    }

    private MergeCommandResult merge() throws IOException {
      checkOutTargetBranch();
      MergeResult result = doMergeInClone();
      if (result.getMergeStatus().isSuccessful()) {
        doCommit();
        push();
        return MergeCommandResult.success();
      } else {
        return analyseFailure(result);
      }
    }

    private void checkOutTargetBranch() {
      try {
        clone.checkout().setName(target).call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not checkout target branch for merge: " + target, e);
      }
    }

    private MergeResult doMergeInClone() throws IOException {
      MergeResult result;
      try {
        result = clone.merge()
          .setCommit(false) // we want to set the author manually
          .include(toMerge, resolveRevision(toMerge))
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + toMerge + " into " + target, e);
      }
      return result;
    }

    private void doCommit() {
      logger.debug("merged branch {} into {}", toMerge, target);
      Person authorToUse = determineAuthor();
      try {
        clone.commit()
          .setAuthor(authorToUse.getName(), authorToUse.getMail())
          .setMessage(MessageFormat.format(determineMessageTemplate(), toMerge, target))
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not commit merge between branch " + toMerge + " and " + target, e);
      }
    }

    private String determineMessageTemplate() {
      if (Strings.isNullOrEmpty(messageTemplate)) {
        return MERGE_COMMIT_MESSAGE_TEMPLATE;
      } else {
        return messageTemplate;
      }
    }

    private Person determineAuthor() {
      if (author == null) {
        Subject subject = SecurityUtils.getSubject();
        User user = subject.getPrincipals().oneByType(User.class);
        String name = user.getDisplayName();
        String email = user.getMail();
        logger.debug("no author set; using logged in user: {} <{}>", name, email);
        return new Person(name, email);
      } else {
        return author;
      }
    }

    private void push() {
      try {
        clone.push().call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not push merged branch " + toMerge + " to origin", e);
      }
      logger.debug("pushed merged branch {}", target);
    }

    private MergeCommandResult analyseFailure(MergeResult result) {
      logger.info("could not merged branch {} into {} due to conflict in paths {}", toMerge, target, result.getConflicts().keySet());
      return MergeCommandResult.failure(result.getConflicts().keySet());
    }

    private ObjectId resolveRevision(String branchToMerge) throws IOException {
      ObjectId resolved = clone.getRepository().resolve(branchToMerge);
      if (resolved == null) {
        return clone.getRepository().resolve("origin/" + branchToMerge);
      } else {
        return resolved;
      }
    }
  }
}