package com.zpj.rxlife.livedata;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author genius
 * @date 2019/11/3
 */
abstract class AbsLiveDataObserver<T> extends LiveData<T> implements Observer<T>, Disposable {
    private boolean isActive;
    private boolean isExecute = false;
    private T data;

    AbsLiveDataObserver(final LifecycleOwner lifecycleOwner) {
        if (!isMain()) {
            AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    if (!isDisposed()
                            && lifecycleOwner.getLifecycle() != null
                            && lifecycleOwner.getLifecycle().getCurrentState() != null) {
                        observe(lifecycleOwner, AbsLiveDataObserver.this);
                    }
                }
            });
        } else {
            observe(lifecycleOwner, this);
        }
    }

    @Override
    protected void onInactive() {
        isActive = false;
    }

    @Override
    protected void onActive() {
        isActive = true;
        realOnNext();
    }

    void onLiveNext(T data) {
        isExecute = true;
        this.data = data;
        realOnNext();
    }

    private void realOnNext() {
        if (isExecute && isActive) {
            isExecute = false;
            if (isMain()) {
                setValue(data);
            } else {
                postValue(data);
            }
        }
    }

    @Override
    public void removeObservers(@NonNull final LifecycleOwner owner) {
        if (isMain()) {
            super.removeObservers(owner);
            return;
        }
        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                AbsLiveDataObserver.super.removeObservers(owner);
            }
        });
    }

    private boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
