package jp.kusumotolab.finergit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class FinerRepo {

  private static final Logger log = LoggerFactory.getLogger(FinerRepo.class);

  public final Path path;
  private Git git;

  public FinerRepo(final Path path) {
    log.debug("enter FinerRepo(Path=\"{}\")", path.toString());
    this.path = path;
    this.git = null;
  }

  public boolean initialize() {
    log.trace("enter initialize()");

    try {
      this.git = Git.init()
          .setDirectory(this.path.toFile())
          .setBare(false)
          .call();
    } catch (final Exception e) {
      log.error("git-init command failed, path<{}>", this.path.toString());
      log.error(e.getMessage());
      return false;
    }

    final Repository repository = this.git.getRepository();
    final StoredConfig config = repository.getConfig();
    config.setInt("merge", null, "renamelimit", 999999);

    try {
      config.save();
    } catch (final IOException e) {
      log.error("failed to change the finer repository configuration");
      log.error(e.getMessage());
      return false;
    }

    return true;
  }

  public boolean doCheckoutCommand(final String branchName, final boolean create,
      final RevCommit startPoint, final boolean orphan) {
    log.trace(
        "enter doCheckoutCommand(String=\"{}\", boolean=\"{}\", RevCommit=\"{}\", boolean=\"{}\")",
        branchName, create, RevCommitUtil.getAbbreviatedID(startPoint), orphan);

    final CheckoutCommand checkoutCommand = this.git.checkout();
    checkoutCommand.setCreateBranch(create)
        .setName(branchName)
        .setStartPoint(startPoint)
        .setOrphan(orphan);
    try {
      checkoutCommand.call();
      return true;
    } catch (final Exception e) {
      log.error(
          "git-checkout command failed, branchName <{}>, create <{}>, startPoint <{}>, orphan <{}>, Exception.getMessage <{}>",
          branchName, create, RevCommitUtil.getAbbreviatedID(startPoint), orphan, e.getMessage());
      log.error(e.getMessage());
      return false;
    }
  }

  public boolean doAddCommand(final Collection<String> paths) {
    log.trace("enter doAddCommand(Collection<String>=\"{}\")", paths.size());

    // if paths is empty, do nothing
    if (null == paths || paths.isEmpty()) {
      return false;
    }

    final AddCommand addCommand = this.git.add();
    paths.stream()
        .forEach(addCommand::addFilepattern);
    try {
      addCommand.call();
      return true;
    } catch (final Exception e) {
      log.error("git-add command failed");
      log.error(e.getMessage());
      return false;
    }
  }

  public boolean doRmCommand(final Collection<String> paths) {
    log.trace("enter doRmCommand(Collection<String>=\"{}\")", paths.size());

    // if paths is empty, do nothing
    if (null == paths || paths.isEmpty()) {
      return false;
    }

    final RmCommand rmCommand = this.git.rm();
    paths.stream()
        .forEach(rmCommand::addFilepattern);
    try {
      rmCommand.call();
      return true;
    } catch (final Exception e) {
      log.error("git-rm command failed");
      log.error(e.getMessage());
      return false;
    }
  }

  public RevCommit doCommitCommand(final PersonIdent personIdent, final String originalCommitID,
      final String originalCommitMessage) {
    log.trace("enter doCommitCommand(PersonIdent=\"{}\", String=\"{}\", String=\"{}\")",
        personIdent.toExternalString(), originalCommitID, originalCommitMessage);

    final CommitCommand commitCommand = this.git.commit();
    final String message = "<OriginalCommitID:" + originalCommitID + "> " + originalCommitMessage;
    try {
      final RevCommit commit = commitCommand.setAuthor(personIdent)
          .setMessage(message)
          .call();
      return commit;
    } catch (final Exception e) {
      log.error(
          "git-commit command failed, personIdent<{}>, originalCommitID<{}>, originalCommitMessage<{}>",
          personIdent.toExternalString(), originalCommitID, originalCommitMessage);
      log.error(e.getMessage());
      return null;
    }
  }

  public MergeStatus doMergeCommand(final RevCommit targetCommit) {
    log.trace("enter doMergeCommand(RevCommit=\"{}\")",
        RevCommitUtil.getAbbreviatedID(targetCommit));

    final MergeCommand mergeCommit = this.git.merge();
    try {
      final MergeResult mergeResult = mergeCommit.include(targetCommit)
          .setCommit(false)
          .setFastForward(FastForwardMode.NO_FF)
          .call();
      return mergeResult.getMergeStatus();
    } catch (final Exception e) {
      log.error("git-merge command failed");
      log.error(e.getMessage());
      return null;
    }
  }

  public Status doStatusCommand() {
    log.trace("enter doStatusCommand()");

    final StatusCommand statusCommand = this.git.status();
    try {
      final Status status = statusCommand.call();
      return status;
    } catch (final NoWorkTreeException | GitAPIException e) {
      log.error("git-status command failed");
      log.error(e.getMessage());
      return null;
    }
  }
}
