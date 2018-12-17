package finergit;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.ast.FinerJavaFileBuilder;
import finergit.ast.FinerJavaModule;
import finergit.util.RevCommitUtil;
import jp.ac.titech.c.se.stein.core.ConcurrentRepositoryRewriter;
import jp.ac.titech.c.se.stein.core.EntrySet;
import jp.ac.titech.c.se.stein.core.EntrySet.Entry;
import jp.ac.titech.c.se.stein.core.EntrySet.EntryList;

public class FinerGitRewriter extends ConcurrentRepositoryRewriter {
  private static final Logger log = LoggerFactory.getLogger(FinerGitRewriter.class);

  private final FinerGitConfig config;

  private final FinerJavaFileBuilder builder;

  public FinerGitRewriter(final FinerGitConfig config) {
    this.config = config;
    this.builder = new FinerJavaFileBuilder(config);
    setConcurrent(config.isParallel());
    setPathSensitive(true);
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
      return config.isOtherFilesIncluded() ? super.rewriteEntry(entry) : Entry.EMPTY;
    }

    // Convert to finer modules
    final EntryList result = new EntryList();
    if (config.isOriginalJavaIncluded()) {
      log.debug("Keep original file: {}", entry);
      result.add(entry);
    }
    for (final FinerJavaModule m : extractFinerModules(entry)) {
      final String finerSource = String.join(System.lineSeparator(), m.getLines());
      final ObjectId newId = writeBlob(finerSource.getBytes(StandardCharsets.UTF_8));
      final String name = m.getFileName();
      log.debug("Generate finer module: {} -> {} {}", entry, name, newId.name());
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
