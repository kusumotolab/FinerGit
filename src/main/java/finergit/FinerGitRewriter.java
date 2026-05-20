package finergit;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.ast.FinerJavaFileBuilder;
import finergit.ast.FinerJavaModule;
import finergit.util.RevCommitUtil;
import jp.ac.titech.c.se.stein.core.Context;
import jp.ac.titech.c.se.stein.entry.AnyColdEntry;
import jp.ac.titech.c.se.stein.entry.Entry;
import jp.ac.titech.c.se.stein.rewriter.RepositoryRewriter;

public class FinerGitRewriter extends RepositoryRewriter {

  private static final Logger log = LoggerFactory.getLogger(FinerGitRewriter.class);

  private final FinerGitConfig config;

  private final FinerJavaFileBuilder builder;

  public FinerGitRewriter(final FinerGitConfig config) {
    this.config = config;
    this.builder = new FinerJavaFileBuilder(config);
    final jp.ac.titech.c.se.stein.Application.Config steinConfig =
        new jp.ac.titech.c.se.stein.Application.Config();
    steinConfig.nthreads = config.getNumberOfThreads();
    setConfig(steinConfig);
    this.isPathSensitive = true;
  }

  @Override
  protected String rewriteCommitMessage(final String message, final Context c) {
    return "<OriginalCommitID:" + RevCommitUtil.getAbbreviatedID(c.getCommit()) + "> " + message;
  }

  @Override
  protected AnyColdEntry rewriteEntry(final Entry entry, final Context c) {
    if (entry.isTree()) {
      return super.rewriteEntry(entry, c);
    }

    // Treats non-java files
    if (!entry.name.endsWith(".java")) {
      return config.isOtherFilesIncluded() ? super.rewriteEntry(entry, c) : AnyColdEntry.empty();
    }

    // Convert to finer modules
    final AnyColdEntry.Set result = AnyColdEntry.set();
    if (config.isOriginalJavaIncluded()) {
      log.debug("Keep original file: {} {}", entry, c);
      result.add(entry);
    }
    for (final FinerJavaModule m : extractFinerModules(entry, c)) {
      final String finerSource = // 最終行に改行を入れないと途中行とのマッチングが正しく行われない
          String.join(System.lineSeparator(), m.getLines()) + System.lineSeparator();
      final ObjectId newId = target.writeBlob(finerSource.getBytes(StandardCharsets.UTF_8), c);
      final String name = m.getFileName();
      log.debug("Generate finer module: {} -> {} {} {}", entry, name, newId.name(), c);
      result.add(Entry.of(entry.mode, name, newId, entry.directory));
    }
    return result;
  }

  protected List<FinerJavaModule> extractFinerModules(final Entry entry, final Context c) {
    final String base = entry.directory + "/" + entry.name;
    final String body = new String(source.readBlob(entry.id), StandardCharsets.UTF_8);
    return builder.getFinerJavaModules(base, body);
  }
}
