/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zpj.rxlife;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

import org.reactivestreams.Publisher;

import java.util.concurrent.CancellationException;

/**
 * Transformer that continues a subscription until a second Observable emits an event.
 */
public class LifecycleTransformer<T>
        implements ObservableTransformer<T, T>, FlowableTransformer<T, T>,
        SingleTransformer<T, T>, MaybeTransformer<T, T>, CompletableTransformer {

    private static final Function<Object, Completable> CANCEL_COMPLETABLE =
            new Function<Object, Completable>() {
                @Override
                public Completable apply(@NonNull Object ignore) throws Exception {
                    return Completable.error(new CancellationException());
                }
            };

    final protected Observable<?> observable;

    LifecycleTransformer(Observable<?> observable) {
        this.observable = observable;
    }

    @NonNull
    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream.takeUntil(observable);
    }

    @NonNull
    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        return upstream.takeUntil(observable.toFlowable(BackpressureStrategy.LATEST));
    }

    @NonNull
    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        return upstream.takeUntil(observable.firstOrError());
    }

    @NonNull
    @Override
    public MaybeSource<T> apply(Maybe<T> upstream) {
        return upstream.takeUntil(observable.firstElement());
    }

    @NonNull
    @Override
    public CompletableSource apply(@NonNull Completable upstream) {
        return Completable.ambArray(upstream,
                observable.flatMapCompletable(CANCEL_COMPLETABLE));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LifecycleTransformer<?> that = (LifecycleTransformer<?>) o;

        return observable.equals(that.observable);
    }

    @Override
    public int hashCode() {
        return observable.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "LifecycleTransformer{" + "observable=" + observable + '}';
    }
}