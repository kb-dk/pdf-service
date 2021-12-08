package com.google.common.util.concurrent;

import com.google.common.base.Supplier;


import javax.annotation.Nonnull;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Guava Striped Factory methods. Just a wrapper around Guava Striped
 *
 * Made this to expose the Supplier, which allows us to use Fair locks.
 *
 * See for how long Google have ignored this feature
 * <ul>
 * <li>https://github.com/google/guava/pull/2515</li>
 * <li>https://github.com/google/guava/issues/2514</li>
 * <li>https://github.com/google/guava/issues/1893</li>
 * </ul>
 *
 *
 * Must exist in this package to use the Striped classes with package-private access
 *
 * @see Striped
 */
public abstract class StripedFactory {
    /**
     * If there are at least this many stripes, we assume the memory usage of a ConcurrentMap will be
     * smaller than a large array. (This assumes that in the lazy case, most stripes are unused. As
     * always, if many stripes are in use, a non-lazy striped makes more sense.)
     *
     * @see Striped#LARGE_LAZY_CUTOFF which is private so we have to redeclare it here...
     */
    private static final int LARGE_LAZY_CUTOFF = 1024;
    
    
    // Static factories
    
    /**
     * Creates a {@code Striped<L>} with eagerly initialized, strongly referenced locks. Every lock is
     * obtained from the passed supplier.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @param supplier a {@code Supplier<L>} object to obtain locks from
     * @return a new {@code Striped<L>}
     */
    protected static <L> Striped<L> custom(int stripes, Supplier<L> supplier) {
        return Striped.custom(stripes, supplier);
    }
    
    
    
    protected static <L> Striped<L> lazy(int stripes, Supplier<L> supplier) {
        //This is not package-private so we cannot just call Striped.lazy :(
        return stripes < LARGE_LAZY_CUTOFF
               ? new Striped.SmallLazyStriped<L>(stripes, supplier)
               : new Striped.LargeLazyStriped<L>(stripes, supplier);
    }
    
    /*Locks*/
    
    /**
     * Creates a {@code Striped<Lock>} with eagerly initialized, strongly referenced locks. Every lock
     * is reentrant.
     * The Lock will NOT be fair
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> lock(int stripes) {
        return Striped.lock(stripes);
    }
    
    /**
     * Creates a {@code Striped<Lock>} with eagerly initialized, strongly referenced locks. Every lock
     * is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> lock(int stripes, boolean fair) {
        return Striped.lock(stripes);
    }
    
    /**
     * Creates a {@code Striped<Lock>} with lazily initialized, weakly referenced locks. Every lock is
     * reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> lockLazyWeak(int stripes) {
        return Striped.lazyWeakLock(stripes);
    }
    
    /**
     * Creates a {@code Striped<Lock>} with lazily initialized, weakly referenced locks. Every lock is
     * reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> lockLazyWeak(int stripes, boolean fair) {
        return lazy(stripes, () -> new ReentrantLock(fair));
    }
    
    
    /* Semaphores*/
    
    /**
     * Creates a {@code Striped<Semaphore>} with eagerly initialized, strongly referenced semaphores,
     * with the specified number of permits.
     *
     *  The semaphore will NOT be fair
     * @param stripes the minimum number of stripes (semaphores) required
     * @param permits the number of permits in each semaphore
     * @return a new {@code Striped<Semaphore>}
     */
    public static Striped<Semaphore> semaphore(int stripes, final int permits) {
        return Striped.semaphore(stripes, permits);
    }
    
    
    /**
     * Creates a {@code Striped<Semaphore>} with lazily initialized, weakly referenced semaphores,
     * with the specified number of permits.
     *
     * @param stripes the minimum number of stripes (semaphores) required
     * @param permits the number of permits in each semaphore
     * @return a new {@code Striped<Semaphore>}
     */
    public static Striped<Semaphore> semaphoreLazyWeak(int stripes, final int permits) {
        return Striped.lazyWeakSemaphore(stripes,permits);
    }
    
    /**
     * Creates a {@code Striped<Semaphore>} with lazily initialized, weakly referenced semaphores,
     * with the specified number of permits.
     *
     * @param stripes the minimum number of stripes (semaphores) required
     * @param permits the number of permits in each semaphore
     * @param fair should the semaphores be fair
     * @return a new {@code Striped<Semaphore>}
     */
    public static Striped<Semaphore> semaphoreLazyWeak(int stripes, final int permits, boolean fair) {
        return lazy(stripes, () -> new Semaphore(permits, fair));
    }
    
    
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with eagerly initialized, strongly referenced
     * read-write locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> readWriteLock(int stripes) {
        return Striped.custom(stripes, () -> new ReentrantReadWriteLock(false));
    }
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with eagerly initialized, strongly referenced
     * read-write locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @param fair is the lock should be fair
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> readWriteLock(int stripes, boolean fair) {
        return Striped.custom(stripes, () -> new ReentrantReadWriteLock(fair));
    }
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with lazily initialized, weakly referenced read-write
     * locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> readWriteLockLazyWeak(int stripes) {
        return Striped.lazyWeakReadWriteLock(stripes);
    }
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with lazily initialized, weakly referenced read-write
     * locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @param fair should the locks be fair
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> readWriteLockLazyWeak(int stripes, boolean fair) {
        return lazy(stripes, () -> new WeakSafeReadWriteLock(fair));
    }
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with lazily initialized, weakly referenced read-write
     * locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @param supplier the supplier of locks. Should really use WeakSafeReadWriteLock
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> readWriteLockLazyWeak(int stripes, Supplier<ReadWriteLock> supplier) {
        return lazy(stripes, supplier);
    }
    
    
    
    /* These classes were private and where needed for the lazyWeakReadWriteLock to be safe for weak references*/
    /**
     * ReadWriteLock implementation whose read and write locks retain a reference back to this lock.
     * Otherwise, a reference to just the read lock or just the write lock would not suffice to ensure
     * the {@code ReadWriteLock} is retained.
     */
    public static final class WeakSafeReadWriteLock implements ReadWriteLock {
        private final ReadWriteLock delegate;
        
        public WeakSafeReadWriteLock() {
            this.delegate = new ReentrantReadWriteLock();
        }
    
        public WeakSafeReadWriteLock(boolean fair) {
            this.delegate = new ReentrantReadWriteLock(fair);
        }
        
        @Override
        @Nonnull
        public Lock readLock() {
            return new WeakSafeLock(delegate.readLock(), this);
        }
        
        @Override
        @Nonnull
        public Lock writeLock() {
            return new WeakSafeLock(delegate.writeLock(), this);
        }
    }
    
    /** Lock object that ensures a strong reference is retained to a specified object. */
    public static final class WeakSafeLock extends ForwardingLock {
        private final Lock delegate;
        
        @SuppressWarnings("unused")
        private final WeakSafeReadWriteLock strongReference;
        
        public WeakSafeLock(Lock delegate, WeakSafeReadWriteLock strongReference) {
            this.delegate = delegate;
            this.strongReference = strongReference;
        }
        
        @Override
        @Nonnull
        Lock delegate() {
            return delegate;
        }
        
        @Override
        @Nonnull
        public Condition newCondition() {
            return new WeakSafeCondition(delegate.newCondition(), strongReference);
        }
    }
    
    /** Condition object that ensures a strong reference is retained to a specified object. */
    public static final class WeakSafeCondition extends ForwardingCondition {
        private final Condition delegate;
        
        @SuppressWarnings("unused")
        private final WeakSafeReadWriteLock strongReference;
        
        public WeakSafeCondition(Condition delegate, WeakSafeReadWriteLock strongReference) {
            this.delegate = delegate;
            this.strongReference = strongReference;
        }
        
        @Override
        @Nonnull
        Condition delegate() {
            return delegate;
        }
    }
    
}
