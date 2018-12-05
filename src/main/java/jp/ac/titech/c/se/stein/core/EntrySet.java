package jp.ac.titech.c.se.stein.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Abstract tree entry.
 */
public interface EntrySet {
    void registerTo(List<Entry> out);

    @SuppressWarnings("unchecked")
    public static final List<Entry> EMPTY_ENTRIES = Collections.EMPTY_LIST;

    public static final EmptySet EMPTY = new EmptySet();

    /**
     * A normal tree entry.
     */
    public static class Entry implements EntrySet {
        public final FileMode mode;

        public final String name;

        public final ObjectId id;

        public final String pathContext;

        public Entry(final FileMode mode, final String name, final ObjectId id, final String pathContext) {
            this.mode = mode;
            this.name = name;
            this.id = id;
            this.pathContext = pathContext;
        }

        @Override
        public String toString() {
            if (pathContext != null) {
                return String.format("%s %s %s", mode, name, id);
            } else {
                return String.format("%s %s/%s %s", mode, pathContext, name, id);
            }
        }

        public boolean isTree() {
            return FileMode.TREE.equals(mode.getBits());
        }

        @Override
        public void registerTo(final List<Entry> out) {
            out.add(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (id == null ? 0 : id.hashCode());
            result = prime * result + (mode == null ? 0 : mode.hashCode());
            result = prime * result + (name == null ? 0 : name.hashCode());
            result = prime * result + (pathContext == null ? 0 : pathContext.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (mode == null) {
                if (other.mode != null) {
                    return false;
                }
            } else if (!mode.equals(other.mode)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (pathContext == null) {
                if (other.pathContext != null) {
                    return false;
                }
            } else if (!pathContext.equals(other.pathContext)) {
                return false;
            }
            return true;
        }
    }

    /**
     * A set of multiple tree entries.
     */
    public static class EntryList implements EntrySet {

        private final List<Entry> entries = new ArrayList<>();

        public EntryList() {
        }

        public List<Entry> entries() {
            return entries;
        }

        public void add(final Entry entry) {
            entries.add(entry);
        }

        @Override
        public String toString() {
            return entries.toString();
        }

        @Override
        public void registerTo(final List<Entry> out) {
            out.addAll(entries);
        }

        @Override
        public int hashCode() {
            return entries.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EntryList other = (EntryList) obj;
            if (entries == null) {
                if (other.entries != null) {
                    return false;
                }
            } else if (!entries.equals(other.entries)) {
                return false;
            }
            return true;
        }
    }

    /**
     * An empty set of tree entries.
     */
    public static class EmptySet implements EntrySet {
        private EmptySet() {
        }

        @Override
        public void registerTo(final List<Entry> out) {
        }
    }
}
