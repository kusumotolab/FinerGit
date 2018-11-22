package finergit.rewrite;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

/**
 * Abstract tree entry.
 */
public class RefEntry {
    public final String name;

    public final ObjectId id;

    public final String target;

    public static final RefEntry EMPTY = new RefEntry(null, null, null);

    public RefEntry(final String name, final ObjectId id, final String target) {
        this.name = name;
        this.id = id;
        this.target = target;
    }

    public RefEntry(final String name, final ObjectId id) {
        this(name, id, null);
    }

    public RefEntry(final String name, final String target) {
        this(name, null, target);
    }

    public RefEntry(final Ref ref) {
        this(ref.getName(), ref.isSymbolic() ? null : ref.getObjectId(), ref.isSymbolic() ? ref.getTarget().getName() : null);
    }

    public boolean isSymbolic() {
        return target != null;
    }

    @Override
    public String toString() {
        return String.format("%s %s", name, target != null ? target : id.name());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (target == null ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RefEntry other = (RefEntry) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }
}
