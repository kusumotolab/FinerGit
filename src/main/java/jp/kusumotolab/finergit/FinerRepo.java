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
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleWalk.IgnoreSubmoduleMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class FinerRepo {

  private static final Logger log = LoggerFactory.getLogger(FinerRepo.class);

  private Git git;
  public final Path path;
  private final Timer addStopWatch;
  private final Timer checkoutStopWatch;
  private final Timer commitStopWatch;
  private final Timer mergeStopWatch;
  private final Timer rmStopWatch;
  private final Timer statusStopWatch;

  public FinerRepo(final Path path) {
    log.trace("enter FinerRepo(Path=\"{}\")", path.toString());
    this.git = null;
    this.path = path;
    this.addStopWatch = new Timer();
    this.checkoutStopWatch = new Timer();
    this.commitStopWatch = new Timer();
    this.mergeStopWatch = new Timer();
    this.rmStopWatch = new Timer();
    this.statusStopWatch = new Timer();
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

    this.checkoutStopWatch.start();
    final CheckoutCommand checkoutCommand = this.git.checkout()
        .setCreateBranch(create)
        .setName(branchName)
        .setStartPoint(startPoint)
        .setOrphan(orphan);
    boolean success = true;

    try {
      checkoutCommand.call();
    } catch (final Exception e) {
      success = false;
      log.error(
          "git-checkout command failed, branchName <{}>, create <{}>, startPoint <{}>, orphan <{}>",
          branchName, create, RevCommitUtil.getAbbreviatedID(startPoint), orphan);
      log.error(e.getMessage());
    } finally {
      this.checkoutStopWatch.suspend();
    }

    return success;
  }

  public boolean doAddCommand(final Collection<String> paths) {
    log.trace("enter doAddCommand(Collection<String>=\"{}\")", paths.size());

    // if paths is empty, do nothing
    if (null == paths || paths.isEmpty()) {
      return false;
    }

    this.addStopWatch.start();
    final AddCommand addCommand = this.git.add()
        .setUpdate(false);
    paths.forEach(addCommand::addFilepattern);
    boolean success = true;

    try {
      addCommand.call();
    } catch (final Exception e) {
      success = false;
      log.error("git-add command failed");
      log.error(e.getMessage());
    } finally {
      this.addStopWatch.suspend();
    }

    return success;
  }

  public boolean doRmCommand(final Collection<String> paths) {
    log.trace("enter doRmCommand(Collection<String>=\"{}\")", paths.size());

    // if paths is empty, do nothing
    if (null == paths || paths.isEmpty()) {
      return false;
    }

    this.rmStopWatch.start();
    final RmCommand rmCommand = this.git.rm();
    paths.stream()
        .forEach(rmCommand::addFilepattern);
    boolean success = true;

    try {
      rmCommand.call();
    } catch (final Exception e) {
      success = false;
      log.error("git-rm command failed");
      log.error(e.getMessage());
    } finally {
      this.rmStopWatch.suspend();
    }

    return success;
  }

  public RevCommit doCommitCommand(final PersonIdent personIdent, final String originalCommitID,
      final String originalCommitMessage) {
    log.trace("enter doCommitCommand(PersonIdent=\"{}\", String=\"{}\", String=\"{}\")",
        personIdent.toExternalString(), originalCommitID, originalCommitMessage);

    this.commitStopWatch.start();
    final CommitCommand commitCommand = this.git.commit();
    final String message = "<OriginalCommitID:" + originalCommitID + "> " + originalCommitMessage;
    RevCommit commit = null;
    try {
      commit = commitCommand.setAuthor(personIdent)
          .setMessage(message)
          .call();
    } catch (final Exception e) {
      log.error(
          "git-commit command failed, personIdent<{}>, originalCommitID<{}>, originalCommitMessage<{}>",
          personIdent.toExternalString(), originalCommitID, originalCommitMessage);
      log.error(e.getMessage());
    } finally {
      this.commitStopWatch.suspend();
    }

    return commit;
  }

  public MergeStatus doMergeCommand(final RevCommit targetCommit) {
    log.trace("enter doMergeCommand(RevCommit=\"{}\")",
        RevCommitUtil.getAbbreviatedID(targetCommit));

    this.mergeStopWatch.start();
    final MergeCommand mergeCommit = this.git.merge();
    MergeStatus mergeStatus = null;
    try {
      mergeStatus = mergeCommit.include(targetCommit)
          .setCommit(false)
          .setFastForward(FastForwardMode.NO_FF)
          .setStrategy(MergeStrategy.OURS)
          .call()
          .getMergeStatus();
    } catch (final GitAPIException e) {
      log.error("git-merge command failed due to GitAPIException");
      log.error(e.getMessage());
    } catch (final Exception e) {
      log.error("git-merge command failed to unknown reason");
      log.error(e.getMessage());
    } finally {
      this.mergeStopWatch.suspend();
    }

    return mergeStatus;
  }

  public Status doStatusCommand() {
    log.trace("enter doStatusCommand()");

    this.statusStopWatch.start();
    final StatusCommand statusCommand = this.git.status()
        .setIgnoreSubmodules(IgnoreSubmoduleMode.ALL);
    Status status = null;
    try {
      status = statusCommand.call();
    } catch (final NoWorkTreeException | GitAPIException e) {
      log.error("git-status command failed");
      log.error(e.getMessage());
    } finally {
      this.statusStopWatch.suspend();
    }

    return status;
  }

  public String getCurrentBranch() {
    try {
      return this.git.getRepository()
          .getBranch();
    } catch (final IOException e) {
      log.error("failed to access repository \"{}\"", this.path);
      return "";
    }
  }

  public String getAddCommandExecutionTime() {
    return this.addStopWatch.toString();
  }

  public String getCheckoutCommandExcecutionTime() {
    return this.checkoutStopWatch.toString();
  }

  public String getCommitCommandExecutionTime() {
    return this.commitStopWatch.toString();
  }

  public String getMergeCommandExecutionTime() {
    return this.mergeStopWatch.toString();
  }

  public String getRmCommandExecutionTime() {
    return this.rmStopWatch.toString();
  }

  public String getStatusCommandExcutionTime() {
    return this.statusStopWatch.toString();
  }
}
