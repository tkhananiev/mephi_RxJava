// Observable.java
package mephi.rxjava;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Consumer;

public class Observable<T> {
    private Consumer<Observer<T>> onSubscribe;
    private Executor subscribeExecutor = Runnable::run;
    private Executor observeExecutor = Runnable::run;

    private Observable(Consumer<Observer<T>> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(Consumer<Observer<T>> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return create(observer -> Observable.this.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    R mapped = mapper.apply(item);
                    observer.onNext(mapped);
                } catch (Throwable t) {
                    observer.onError(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        }));
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return create(observer -> Observable.this.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item)) {
                        observer.onNext(item);
                    }
                } catch (Throwable t) {
                    observer.onError(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        }));
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return create(observer -> Observable.this.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    Observable<R> newObservable = mapper.apply(item);
                    newObservable.subscribe(new Observer<R>() {
                        @Override
                        public void onNext(R subItem) {
                            observer.onNext(subItem);
                        }

                        @Override
                        public void onError(Throwable t) {
                            observer.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            observer.onComplete();
                        }
                    });
                } catch (Throwable t) {
                    observer.onError(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        }));
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        this.subscribeExecutor = scheduler::execute;
        return this;
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        this.observeExecutor = scheduler::execute;
        return this;
    }

    public Disposable subscribe(Observer<T> observer) {
        SimpleDisposable disposable = new SimpleDisposable();

        subscribeExecutor.execute(() -> {
            if (!disposable.isDisposed()) {
                try {
                    onSubscribe.accept(new Observer<T>() {
                        @Override
                        public void onNext(T item) {
                            if (!disposable.isDisposed()) {
                                observeExecutor.execute(() -> observer.onNext(item));
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            if (!disposable.isDisposed()) {
                                observeExecutor.execute(() -> observer.onError(t));
                            }
                        }

                        @Override
                        public void onComplete() {
                            if (!disposable.isDisposed()) {
                                observeExecutor.execute(observer::onComplete);
                            }
                        }
                    });
                } catch (Throwable t) {
                    observer.onError(t);
                }
            }
        });

        return disposable;
    }
}
