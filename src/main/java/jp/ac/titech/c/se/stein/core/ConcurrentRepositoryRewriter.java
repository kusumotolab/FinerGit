package jp.ac.titech.c.se.stein.core;

import java.util.HashMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ac.titech.c.se.stein.core.Try.ThrowableFunction;

public class ConcurrentRepositoryRewriter extends RepositoryRewriter implements Configurable {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentRepositoryRewriter.class);

    protected boolean concurrent = false;

    protected ConcurrentMap<Thread, ObjectInserter> inserters = null;

    public void setConcurrent(final boolean concurrent) {
        log.debug("Set concurrent: {}", concurrent);
        this.concurrent = concurrent;
        this.entryMapping = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
        log.debug("Concurrent mode: {}", concurrent);
    }

    @Override
    public void addOptions(final Config conf) {
        super.addOptions(conf);
        conf.addOption("c", "concurrent", false, "rewrite trees concurrently");
    }

    @Override
    public void configure(final Config conf) {
        super.configure(conf);
        if (conf.hasOption("concurrent")) {
            setConcurrent(true);
        }
    }

    /**
     * Rewrites all commits.
     */
    @Override
    protected void rewriteCommits() {
        if (concurrent) {
            rewriteTreesConcurrently();
        }
        super.rewriteCommits();
    }

    /**
     * Rewrites all trees concurrently.
     */
    protected void rewriteTreesConcurrently() {
        inserters = new ConcurrentHashMap<>();

        try (final RevWalk walk = prepareRevisionWalk()) {
            final int characteristics = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL;
            final Spliterator<RevCommit> split = Spliterators.spliteratorUnknownSize(walk.iterator(), characteristics);
            final Stream<RevCommit> stream = StreamSupport.stream(split, true);
            stream.forEach(c -> rewriteRootTree(c.getTree().getId()));
        }

        Try.io(() -> {
            for (final ObjectInserter ins : inserters.values()) {
                ins.close();
            }
        });
        inserters = null;
    }

    @Override
    protected <R> R tryInsert(final ThrowableFunction<ObjectInserter, R> f) {
        if (inserters != null) {
            final Thread thread = Thread.currentThread();
            final ObjectInserter ins = inserters.computeIfAbsent(thread, (t) -> writeRepo.newObjectInserter());
            return Try.io(f).apply(ins);
        } else {
            return super.tryInsert(f);
        }
    }
}
