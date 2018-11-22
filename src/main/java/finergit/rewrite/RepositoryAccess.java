package finergit.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefRename;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finergit.rewrite.EntrySet.Entry;
import finergit.rewrite.Try.ThrowableFunction;

public class RepositoryAccess {
    private static final Logger log = LoggerFactory.getLogger(RepositoryAccess.class);

    protected Repository repo;

    protected Repository writeRepo;

    protected boolean overwrite = true;

    public RepositoryAccess() {
    }

    public void initialize(final Repository repo) {
        initialize(repo, repo);
    }

    public void initialize(final Repository readRepo, final Repository writeRepo) {
        this.repo = readRepo;
        this.writeRepo = writeRepo;
        this.overwrite = repo == writeRepo;
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
        return tryInsert((ins) -> ins.insert(f));
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
        return tryInsert((ins) -> ins.insert(Constants.OBJ_BLOB, data));
    }

    /**
     * Applies ref update.
     */
    protected void applyRefUpdate(final RefEntry entry) {
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
        Try.io(() -> {
            final RefUpdate cmd = writeRepo.getRefDatabase().newUpdate(entry.name, false);
            cmd.setForceUpdate(true);
            cmd.delete();
        });
    }

    /**
     * Applies ref rename.
     */
    protected void applyRefRename(final String name, final String newName) {
        Try.io(() -> {
            final RefRename cmd = writeRepo.getRefDatabase().newRename(name, newName);
            cmd.rename();
        });
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
