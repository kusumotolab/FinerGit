package jp.kusumotolab.finergit;

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
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class FinerRepo {

  public final Path path;
  private Git git;

  public FinerRepo(final Path path) {
    this.path = path;
    this.git = null;
  }

  public boolean initialize() {
    try {
      this.git = Git.init()
          .setDirectory(this.path.toFile())
          .setBare(false)
          .call();
      return true;
    } catch (final Exception e) {
      System.err
          .println("an error happened in initializing a new repository: " + this.path.toString());
      e.printStackTrace();
      return false;
    }
  }

  public boolean doCheckoutCommand(final String branchName, final boolean create,
      final RevCommit startPoint) {
    final CheckoutCommand checkoutCommand = this.git.checkout();
    checkoutCommand.setCreateBranch(create)
        .setName(branchName)
        .setStartPoint(startPoint);
    try {
      checkoutCommand.call();
      return true;
    } catch (final Exception e) {
      System.err.println("git-checkout command failed.");
      e.printStackTrace();
      return false;
    }
  }

  public boolean doAddCommand(final Collection<String> paths) {

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
      System.err.println("git-add command failed.");
      e.printStackTrace();
      return false;
    }
  }

  public boolean doRmCommand(final Collection<String> paths) {

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
      System.err.println("git-rm command failed.");
      e.printStackTrace();
      return false;
    }
  }

  public RevCommit doCommitCommand(final PersonIdent personIdent, final String originalCommitID,
      final String originalCommitMessage) {
    final CommitCommand commitCommand = this.git.commit();
    final String message = "<OriginalCommitID:" + originalCommitID + "> " + originalCommitMessage;
    try {
      final RevCommit commit = commitCommand.setAuthor(personIdent)
          .setMessage(message)
          .call();
      return commit;
    } catch (final Exception e) {
      System.err.println("git-commit command failed.");
      e.printStackTrace();
      return null;
    }
  }

  public MergeStatus doMergeCommand(final RevCommit targetCommit) {
    final MergeCommand mergeCommit = this.git.merge();
    try {
      final MergeResult mergeResult = mergeCommit.include(targetCommit)
          .setCommit(false)
          .setFastForward(FastForwardMode.NO_FF)
          .call();
      return mergeResult.getMergeStatus();
    } catch (final Exception e) {
      System.err.println("git-commit command failed.");
      e.printStackTrace();
      return null;
    }
  }
}
