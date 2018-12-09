package jp.ac.titech.c.se.stein.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefRename;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ac.titech.c.se.stein.core.EntrySet.Entry;
import jp.ac.titech.c.se.stein.core.Try.ThrowableFunction;

public class RepositoryAccess implements Configurable {
    private static final Logger log = LoggerFactory.getLogger(RepositoryAccess.class);

    protected Repository repo;

    protected Repository writeRepo;

    protected boolean overwrite = true;

    protected boolean dryRunning = false;

    public RepositoryAccess() {
    }

    public void setDryRunning(final boolean dryRunning) {
        this.dryRunning = dryRunning;
        log.debug("Dry running mode: {}", dryRunning);
    }

    @Override
    public void addOptions(final Config conf) {
        conf.addOption("n", "dry-run", false, "don't actually write anything");
    }

    @Override
    public void configure(final Config conf) {
        if (conf.hasOption("dry-run")) {
            setDryRunning(true);
        }
    }

    public void initialize(final Repository repo) {
        initialize(repo, repo);
    }

    public void initialize(final Repository readRepo, final Repository writeRepo) {
        this.repo = readRepo;
        this.writeRepo = writeRepo;
        this.overwrite = readRepo == writeRepo;
    }

    /**
     * Specifies the commit that the given ref indicates.
     */
    protected ObjectId specifyCommit(final Ref ref) {
        final Ref peeled = Try.io(() -> repo.getRefDatabase().peel(ref));
        return peeled.getPeeledObjectId() != null ? peeled.getPeeledObjectId() : ref.getObjectId();
    }

    /**
     * Reads a tree object.
     */
    protected List<Entry> readTree(final ObjectId treeId, final String path) {
        final List<Entry> result = new ArrayList<>();
        Try.io(() -> {
            try (final TreeWalk walk = new TreeWalk(repo)) {
                walk.addTree(treeId);
                walk.setRecursive(false);
                while (walk.next()) {
                    result.add(new Entry(walk.getFileMode(), walk.getNameString(), walk.getObjectId(0), path));
                }
            }
        });
        return result;
    }

    /**
     * Writes tree entries to a tree object.
     */
    protected ObjectId writeTree(final Collection<Entry> entries) {
        final TreeFormatter f = new TreeFormatter();
        for (final Entry e : sortEntries(entries)) {
            f.append(e.name, e.mode, e.id);
        }
        return tryInsert((ins) -> dryRunning ? ins.idFor(f) : ins.insert(f));
    }

    /**
     * Sorts tree entries.
     */
    protected Collection<Entry> sortEntries(final Collection<Entry> entries) {
        final SortedMap<String, Entry> map = new TreeMap<>();
        for (final Entry e : entries) {
            final String key = e.name + (e.isTree() ? "/" : "");
            if (map.containsKey(key)) {
                log.warn("Entry occurred twice: {} - {}", map.get(key), e);
            }
            map.put(key, e);
        }
        return map.values();
    }

    /**
     * Reads a blob object.
     */
    protected byte[] readBlob(final ObjectId blobId) {
        return Try.io(() -> repo.getObjectDatabase().open(blobId, Constants.OBJ_BLOB).getBytes());
    }

    /**
     * Writes data to a blob object.
     */
    public ObjectId writeBlob(final byte[] data) {
        return tryInsert((ins) -> dryRunning ? ins.idFor(Constants.OBJ_BLOB, data) : ins.insert(Constants.OBJ_BLOB, data));
    }

    /**
     * Writes a commit object.
     */
    protected ObjectId writeCommit(final ObjectId[] parentIds, final ObjectId treeId, final PersonIdent author, final PersonIdent committer, final String message) {
        final CommitBuilder builder = new CommitBuilder();
        builder.setParentIds(parentIds);
        builder.setTreeId(treeId);
        builder.setAuthor(author);
        builder.setCommitter(committer);
        builder.setMessage(message);
        return tryInsert((ins) -> dryRunning ? ins.idFor(Constants.OBJ_COMMIT, builder.build()) : ins.insert(builder));
    }

    /**
     * Writes a tag object.
     */
    protected ObjectId writeTag(final ObjectId objectId, final String tag, final PersonIdent tagger, final String message) {
        final TagBuilder builder = new TagBuilder();
        builder.setObjectId(objectId, Constants.OBJ_COMMIT);
        builder.setTag(tag);
        builder.setTagger(tagger);
        builder.setMessage(message);
        return tryInsert((ins) -> dryRunning ? ins.idFor(Constants.OBJ_TAG, builder.build()) : ins.insert(builder));
    }

    /**
     * Applies ref update.
     */
    protected void applyRefUpdate(final RefEntry entry) {
        if (!dryRunning) {
            Try.io(() -> {
                final RefUpdate cmd = writeRepo.getRefDatabase().newUpdate(entry.name, false);
                cmd.setForceUpdate(true);
                if (entry.isSymbolic()) {
                    cmd.link(entry.target);
                } else {
                    cmd.setNewObjectId(entry.id);
                    cmd.update();
                }
            });
        }
    }

    /**
     * Tests whether the given ref indicates a tag.
     */
    protected boolean isTag(final Ref ref) {
        final Ref peeled = Try.io(() -> repo.getRefDatabase().peel(ref));
        return peeled.getPeeledObjectId() != null;
    }

    /**
     * Extracts tag object.
     */
    protected AnyObjectId parseAny(final ObjectId id) {
        try (final RevWalk walk = new RevWalk(repo)) {
            return Try.io(() -> walk.parseAny(id));
        }
    }

    /**
     * Extracts tag object.
     */
    protected RevTag parseTag(final ObjectId id) {
        try (final RevWalk walk = new RevWalk(repo)) {
            return Try.io(() -> walk.parseTag(id));
        }
    }

    /**
     * Applies ref delete.
     */
    protected void applyRefDelete(final RefEntry entry) {
        if (!dryRunning) {
            Try.io(() -> {
                final RefUpdate cmd = writeRepo.getRefDatabase().newUpdate(entry.name, false);
                cmd.setForceUpdate(true);
                cmd.delete();
            });
        }
    }

    /**
     * Applies ref rename.
     */
    protected void applyRefRename(final String name, final String newName) {
        if (!dryRunning) {
            Try.io(() -> {
                final RefRename cmd = writeRepo.getRefDatabase().newRename(name, newName);
                cmd.rename();
            });
        }
    }

    /**
     * Prepares an object inserter.
     */
    protected <R> R tryInsert(final ThrowableFunction<ObjectInserter, R> f) {
        try (final ObjectInserter inserter = writeRepo.newObjectInserter()) {
            return Try.io(f).apply(inserter);
        }
    }
}
