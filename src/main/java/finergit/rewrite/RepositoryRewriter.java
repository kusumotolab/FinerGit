package finergit.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finergit.rewrite.EntrySet.Entry;
import finergit.rewrite.Try.ThrowableFunction;

public class RepositoryRewriter extends RepositoryAccess {
    private static final Logger log = LoggerFactory.getLogger(RepositoryRewriter.class);

    private static final ObjectId ZERO = ObjectId.zeroId();

    protected final Map<ObjectId, ObjectId> commitMapping = new HashMap<>();

    protected Map<Entry, EntrySet> entryMapping = new HashMap<>();

    protected Map<RefEntry, RefEntry> refEntryMapping = new HashMap<>();

    protected ObjectInserter inserter = null;

    protected boolean pathSensitive = false;

    public void rewrite() {
        rewriteCommits();
        updateRefs();
    }

    /**
     * Sets whether entries are path-sensitive.
     */
    protected void setPathSensitive(final boolean value) {
        this.pathSensitive = value;
    }

    /**
     * Rewrites all commits.
     */
    protected void rewriteCommits() {
        try (final ObjectInserter ins = writeRepo.newObjectInserter()) {
            this.inserter = ins;
            try (final RevWalk walk = prepareRevisionWalk()) {
                for (final RevCommit c : walk) {
                    rewriteCommit(c);
                }
            }
            this.inserter = null;
        }
    }

    /**
     * Prepares the revision walk.
     */
    protected RevWalk prepareRevisionWalk() {
        final Collection<ObjectId> starts = collectStarts();
        final Collection<ObjectId> uninterestings = collectUninterestings();

        final RevWalk walk = new RevWalk(repo);
        Try.io(() -> {
            for (final ObjectId id : starts) {
                walk.markStart(walk.parseCommit(id));
            }
            for (final ObjectId id : uninterestings) {
                walk.markUninteresting(walk.parseCommit(id));
            }
        });

        walk.sort(RevSort.TOPO, true);
        walk.sort(RevSort.REVERSE, true);
        return walk;
    }

    /**
     * Collects the set of commit Ids used as start points.
     */
    protected Collection<ObjectId> collectStarts() {
        final List<ObjectId> result = new ArrayList<>();
        final List<Ref> refs = Try.io(() -> repo.getRefDatabase().getRefs());
        for (final Ref ref : refs) {
            if (confirmStartRef(ref)) {
                final ObjectId commitId = specifyCommit(ref);
                log.debug("Add start point: {} (specified by {})", commitId.name(), ref.getName());
                result.add(specifyCommit(ref));
            }
        }
        return result;
    }

    /**
     * Confirms whether the given ref is used for a start point.
     */
    protected boolean confirmStartRef(final Ref ref) {
        final String name = ref.getName();
        return name.equals(Constants.HEAD) || name.startsWith(Constants.R_HEADS) || name.startsWith(Constants.R_TAGS);
    }

    /**
     * Collects the set of commit Ids used as uninteresting points.
     */
    protected Collection<ObjectId> collectUninterestings() {
        @SuppressWarnings("unchecked")
        final List<ObjectId> result = Collections.EMPTY_LIST;
        return result;
    }

    /**
     * Rewrites a commit.
     *
     * @param commit
     *            target commit.
     * @return the object ID of the rewritten commit
     */
    protected ObjectId rewriteCommit(final RevCommit commit) {
        final CommitBuilder builder = new CommitBuilder();
        builder.setParentIds(rewriteParents(commit.getParents()));
        builder.setTreeId(rewriteRootTree(commit.getTree().getId()));
        builder.setAuthor(rewriteAuthor(commit.getAuthorIdent(), commit));
        builder.setCommitter(rewriteCommitter(commit.getCommitterIdent(), commit));
        builder.setMessage(rewriteCommitMessage(commit.getFullMessage(), commit));
        final ObjectId newId = tryInsert((i) -> i.insert(builder));

        final ObjectId oldId = commit.getId();
        commitMapping.put(oldId, newId);

        log.debug("Rewrite commit: {} -> {}", oldId.name(), newId.name());
        return newId;
    }

    /**
     * Rewrites the parents of a commit.
     */
    protected ObjectId[] rewriteParents(final ObjectId[] parents) {
        final ObjectId[] result = new ObjectId[parents.length];
        for (int i = 0; i < parents.length; i++) {
            result[i] = commitMapping.get(parents[i]);
        }
        return result;
    }

    /**
     * Rewrites the root tree of a commit.
     */
    protected ObjectId rewriteRootTree(final ObjectId treeId) {
        // A root tree is represented as a special entry whose name is "/"
        final Entry root = new Entry(FileMode.TREE, "/", treeId, pathSensitive ? "" : null);
        final EntrySet newRoot = getEntry(root);
        final ObjectId newId = newRoot == EntrySet.EMPTY ? writeTree(EntrySet.EMPTY_ENTRIES) : ((Entry) newRoot).id;

        log.debug("Rewrite tree: {} -> {}", treeId.name(), newId.name());
        return newId;
    }

    /**
     * Obtains tree entries from a tree entry.
     */
    protected EntrySet getEntry(final Entry entry) {
        // computeIfAbsent is unsuitable because this may be invoked recursively
        final EntrySet cache = entryMapping.get(entry);
        if (cache != null) {
            return cache;
        } else {
            final EntrySet result = rewriteEntry(entry);
            entryMapping.put(entry, result);
            return result;
        }
    }

    /**
     * Rewrites a tree entry.
     */
    protected EntrySet rewriteEntry(final Entry entry) {
        final ObjectId newId = entry.isTree() ? rewriteTree(entry.id, entry) : rewriteBlob(entry.id, entry);
        final String newName = rewriteName(entry.name, entry);
        return newId == ZERO ? EntrySet.EMPTY : new Entry(entry.mode, newName, newId, entry.pathContext);
    }

    /**
     * Rewrites a tree object.
     */
    protected ObjectId rewriteTree(final ObjectId treeId, final Entry entry) {
        final List<Entry> entries = new ArrayList<>();
        for (final Entry e : readTree(treeId, pathSensitive ? entry.pathContext + "/" + entry.name : null)) {
            final EntrySet rewritten = getEntry(e);
            rewritten.registerTo(entries);
        }
        return entries.isEmpty() ? ZERO : writeTree(entries);
    }

    /**
     * Rewrites a blob object.
     */
    protected ObjectId rewriteBlob(final ObjectId blobId, final Entry entry) {
        return blobId;
    }

    /**
     * Rewrites the name of a tree entry.
     */
    protected String rewriteName(final String name, final Entry entry) {
        return name;
    }

    /**
     * Rewrites the author identity of a commit.
     */
    protected PersonIdent rewriteAuthor(final PersonIdent author, final RevCommit commit) {
        return rewritePerson(author);
    }

    /**
     * Rewrites the committer identity of a commit.
     */
    protected PersonIdent rewriteCommitter(final PersonIdent committer, final RevCommit commit) {
        return rewritePerson(committer);
    }

    /**
     * Rewrites a person identity.
     */
    protected PersonIdent rewritePerson(final PersonIdent person) {
        return person;
    }

    /**
     * Rewrites the message of a commit.
     */
    protected String rewriteCommitMessage(final String message, final RevCommit commit) {
        return rewriteMessage(message, commit.getId());
    }

    /**
     * Rewrites a message.
     */
    protected String rewriteMessage(final String message, final ObjectId id) {
        return "orig:" + id.name() + " " + message;
    }

    /**
     * Updates ref objects.
     */
    protected void updateRefs() {
        final List<Ref> refs = Try.io(() -> repo.getRefDatabase().getRefs());
        for (final Ref ref : refs) {
            if (confirmUpdateRef(ref)) {
                updateRef(ref);
            }
        }
    }

    /**
     * Confirms whether the given ref is to be updated.
     */
    protected boolean confirmUpdateRef(final Ref ref) {
        return confirmStartRef(ref);
    }

    /**
     * Updates a ref object.
     */
    protected RefEntry getRefEntry(final RefEntry entry, final Ref ref) {
        final RefEntry cache = refEntryMapping.get(entry);
        if (cache != null) {
            return cache;
        } else {
            final RefEntry result = rewriteRefEntry(entry, ref);
            refEntryMapping.put(entry, result);
            return result;
        }
    }

    /**
     * Updates a ref object.
     */
    protected void updateRef(final Ref ref) {
        final RefEntry oldEntry = new RefEntry(ref);
        final RefEntry newEntry = getRefEntry(oldEntry, ref);
        if (overwrite && !oldEntry.equals(newEntry)) {
            applyRefDelete(oldEntry);
        }
        if (newEntry != RefEntry.EMPTY) {
            applyRefUpdate(newEntry);
        }
    }

    /**
     * Rewrites a ref entry.
     */
    protected RefEntry rewriteRefEntry(final RefEntry entry, final Ref ref) {
        if (entry.isSymbolic()) {
            final String newName = rewriteRefName(entry.name, ref);
            final String newTarget = getRefEntry(new RefEntry(ref.getTarget()), ref.getTarget()).name;
            return new RefEntry(newName, newTarget);
        } else {
            final String newName = rewriteRefName(entry.name, ref);
            final ObjectId newObjectId = rewriteRefObject(entry.id, ref);
            return newObjectId == ZERO ? RefEntry.EMPTY : new RefEntry(newName, newObjectId);
        }
    }

    /**
     * Rewrites the referred object by a ref.
     */
    protected ObjectId rewriteRefObject(final ObjectId id, final Ref ref) {
        if (isTag(ref)) {
            return rewriteTag(id, parseTag(id), ref);
        } else {
            return rewriteReferredCommit(id, ref);
        }
    }

    /**
     * Rewrites the referred commit object by a ref.
     */
    protected ObjectId rewriteReferredCommit(final ObjectId id, final Ref ref) {
        final ObjectId result = commitMapping.get(id);
        return result != null ? result : id;
    }

    /**
     * Rewrites a tag object.
     */
    protected ObjectId rewriteTag(final ObjectId tagId, final RevTag tag, final Ref ref) {
        final ObjectId newObjectId = rewriteReferredCommit(tag.getObject(), ref);
        if (newObjectId == ZERO) {
            return ZERO;
        }
        log.debug("Rewrite tag target: {} -> {}", tag.getObject().name(), newObjectId.name());

        final TagBuilder builder = new TagBuilder();
        builder.setObjectId(newObjectId, Constants.OBJ_COMMIT);
        builder.setTag(rewriteTagName(tag.getTagName(), ref));
        builder.setTagger(rewriteTagger(tag.getTaggerIdent(), tag, ref));
        builder.setMessage(rewriteTagMessage(tag.getFullMessage(), tag, ref));

        final ObjectId newId = tryInsert((i) -> i.insert(builder));
        log.debug("Rewrite tag: {} -> {}", tagId.name(), newId.name());
        return newId;
    }

    /**
     * Rewrites the tagger identity of a tag.
     */
    protected PersonIdent rewriteTagger(final PersonIdent tagger, final RevTag tag, final Ref ref) {
        return rewritePerson(tagger);
    }

    /**
     * Rewrites the message of a tag.
     */
    protected String rewriteTagMessage(final String message, final RevTag tag, final Ref ref) {
        return rewriteMessage(message, tag.getId());
    }

    /**
     * Rewrites a ref name.
     */
    protected String rewriteRefName(final String name, final Ref ref) {
        if (name.startsWith(Constants.R_HEADS)) {
            final String branchName = name.substring(Constants.R_HEADS.length());
            return Constants.R_HEADS + rewriteBranchName(branchName, ref);
        } else if (name.startsWith(Constants.R_TAGS)) {
            final String tagName = name.substring(Constants.R_TAGS.length());
            return Constants.R_TAGS + rewriteTagName(tagName, ref);
        } else {
            return name;
        }
    }

    /**
     * Rewrites a local branch name.
     */
    protected String rewriteBranchName(final String name, final Ref ref) {
        return name;
    }

    /**
     * Rewrites a tag name.
     */
    protected String rewriteTagName(final String name, final Ref ref) {
        return name;
    }

    @Override
    protected <R> R tryInsert(final ThrowableFunction<ObjectInserter, R> f) {
        if (inserter != null) {
            return Try.io(f).apply(inserter);
        } else {
            return super.tryInsert(f);
        }
    }
}
