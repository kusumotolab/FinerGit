package finergit;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import finergit.ast.FinerJavaFileBuilder;
import finergit.ast.FinerJavaModule;
import finergit.rewrite.ConcurrentRepositoryRewriter;
import finergit.rewrite.EntrySet;
import finergit.rewrite.RefEntry;
import finergit.rewrite.EntrySet.Entry;
import finergit.rewrite.EntrySet.EntryList;
import finergit.util.RevCommitUtil;

public class FinerGitRewriter extends ConcurrentRepositoryRewriter {

  private final FinerGitConfig config;

  private final FinerJavaFileBuilder builder;

  private final ObjectId head;

  public FinerGitRewriter(final FinerGitConfig config, final Repository src, final Repository dst, final ObjectId head) {
    this.config = config;
    this.builder = new FinerJavaFileBuilder(config);
    this.head = head;
    setConcurrent(true);
    setPathSensitive(true);
    initialize(src, dst);
  }

  @Override
  protected Collection<ObjectId> collectStarts() {
    return List.of(head);
  }

  public static final String MASTER = Constants.R_HEADS + "master";

  @Override
  protected void updateRefs() {
    applyRefUpdate(new RefEntry(MASTER, rewriteReferredCommit(head, null)));
    applyRefUpdate(new RefEntry(Constants.HEAD, MASTER));
  }

  @Override
  protected String rewriteCommitMessage(final String message, final RevCommit commit) {
    return "<OriginalCommitID:" + RevCommitUtil.getAbbreviatedID(commit) + "> " + message;
  }

  @Override
  public EntrySet rewriteEntry(final Entry entry) {
    if (entry.isTree()) {
      return super.rewriteEntry(entry);
    }

    // Treats non-java files
    if (!entry.name.endsWith(".java")) {
      if (config.isOtherFilesIncluded()) {
        return new Entry(entry.mode, entry.name, writeBlob(readBlob(entry.id)), entry.pathContext);
      } else {
        return Entry.EMPTY;
      }
    }

    // Convert to finer modules
    final EntryList result = new EntryList();
    if (config.isOriginalJavaIncluded()) {
      result.add(entry);
    }
    for (final FinerJavaModule m : extractFinerModules(entry)) {
      final String finerSource = String.join(System.lineSeparator(), m.getLines());
      final ObjectId newId = writeBlob(finerSource.getBytes(StandardCharsets.UTF_8));
      final String name = m.getFileName();
      result.add(new Entry(entry.mode, name, newId, entry.pathContext));
    }
    return result;
  }

  protected List<FinerJavaModule> extractFinerModules(final Entry entry) {
    final String base = entry.pathContext + "/" + entry.name;
    final String source = new String(readBlob(entry.id), StandardCharsets.UTF_8);
    return builder.getFinerJavaModules(base, source);
  }
}
