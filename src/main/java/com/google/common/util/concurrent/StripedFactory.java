package com.google.common.util.concurrent;

import com.google.common.base.Supplier;


import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

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
 * @see Striped
 */
public abstract class StripedFactory {
    /**
     * If there are at least this many stripes, we assume the memory usage of a ConcurrentMap will be
     * smaller than a large array. (This assumes that in the lazy case, most stripes are unused. As
     * always, if many stripes are in use, a non-lazy striped makes more sense.)
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
    public static <L> Striped<L> custom(int stripes, Supplier<L> supplier) {
        return Striped.custom(stripes, supplier);
    }
    
    /**
     * Creates a {@code Striped<Lock>} with eagerly initialized, strongly referenced locks. Every lock
     * is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> lock(int stripes) {
        return Striped.lock(stripes);
    }
    
    
    /**
     * Creates a {@code Striped<Lock>} with lazily initialized, weakly referenced locks. Every lock is
     * reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> lazyWeakLock(int stripes) {
        return Striped.lazyWeakLock(stripes);
    }
    
    public static <L> Striped<L> lazyWeakLock(int stripes, Supplier<L> supplier) {
        return stripes < LARGE_LAZY_CUTOFF
               ? new Striped.SmallLazyStriped<L>(stripes, supplier)
               : new Striped.LargeLazyStriped<L>(stripes, supplier);
    }
    
    /**
     * Creates a {@code Striped<Semaphore>} with eagerly initialized, strongly referenced semaphores,
     * with the specified number of permits.
     *
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
    public static Striped<Semaphore> lazyWeakSemaphore(int stripes, final int permits) {
        return Striped.lazyWeakSemaphore(stripes,permits);
    }
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with eagerly initialized, strongly referenced
     * read-write locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> readWriteLock(int stripes) {
        return Striped.readWriteLock(stripes);
    }
    
    /**
     * Creates a {@code Striped<ReadWriteLock>} with lazily initialized, weakly referenced read-write
     * locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> lazyWeakReadWriteLock(int stripes) {
        return Striped.lazyWeakReadWriteLock(stripes);
    }
    /**
     * Creates a {@code Striped<ReadWriteLock>} with lazily initialized, weakly referenced read-write
     * locks. Every lock is reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<ReadWriteLock>}
     */
    public static Striped<ReadWriteLock> lazyWeakReadWriteLock(int stripes,  Supplier<ReadWriteLock> supplier) {
        return lazyWeakLock(stripes, supplier);
    }
}
