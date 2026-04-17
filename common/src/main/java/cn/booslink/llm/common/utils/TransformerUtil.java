package cn.booslink.llm.common.utils;

import android.database.sqlite.SQLiteFullException;

import androidx.core.util.Pair;

import com.google.gson.JsonParseException;

import org.reactivestreams.Publisher;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.booslink.llm.common.model.ApiResponse;
import cn.booslink.llm.common.network.exception.ApiException;
import cn.booslink.llm.common.network.exception.NoConnectivityException;
import cn.booslink.llm.common.network.exception.PingPublicNetException;
import cn.booslink.llm.common.network.exception.RetryFailException;
import cn.booslink.llm.common.network.exception.ServerUnReachableException;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import timber.log.Timber;

public class TransformerUtil {
    private static final String TAG = "RetryUtil";

    public static <T> SingleTransformer<ApiResponse<T>, T> singleApiTransformer() {
        return upstream -> upstream.map(tApiResponse -> {
            if (tApiResponse.isSuccess()) {
                return tApiResponse.getData();
            } else {
                throw new ApiException(tApiResponse.getMessage());
            }
        });
    }

    public static <T> SingleTransformer<T, T> singleApiRetry() {
        return upstream -> upstream.retryWhen(throwableFlowable ->
                throwableFlowable
                        .zipWith(Flowable.just(1, 2, 4, 8), ThrowableWithTime::new)
                        .flatMap((Function<ThrowableWithTime, Publisher<?>>) eWithTime -> {
                            if (needRetry(eWithTime.throwable())) {
                                Timber.tag(TAG).d("Api request try after delay: %s", eWithTime.time());
                                return eWithTime.time() == 8 ? Flowable.error(new RetryFailException(eWithTime.throwable())) : Flowable.timer(eWithTime.time(), TimeUnit.SECONDS);
                            }
                            return Flowable.error(eWithTime.throwable());
                        }));
    }

    public static <T> SingleTransformer<T, T> singleCustomRetry(List<Long> retryConditions) { // Arrays.asList(1, 2, 4, 8)
        return upstream -> upstream
                .retryWhen(throwableFlowable ->
                        throwableFlowable
                                .zipWith(retryConditions, (BiFunction<Throwable, Long, Pair<Throwable, Long>>) Pair::new)
                                .flatMap((Function<Pair<Throwable, Long>, Publisher<?>>) throwableLongPair -> {
                                    Throwable throwable = throwableLongPair.first;
                                    long time = throwableLongPair.second;
                                    if (needRetry(throwable)) {
                                        Timber.tag(TAG).d("Api request try after delay: %s", time);
                                        return time == retryConditions.get(retryConditions.size() - 1) ? Flowable.error(new RetryFailException(throwable)) : Flowable.timer(time, TimeUnit.SECONDS);
                                    }
                                    return Flowable.error(throwable);
                                }));
    }

    public static <T> SingleTransformer<T, T> singleDbRetry(List<Long> retryConditions, Runnable runnable) { // Arrays.asList(1, 2, 4, 8)
        return upstream -> upstream
                .retryWhen(throwableFlowable ->
                        throwableFlowable
                                .zipWith(retryConditions, (BiFunction<Throwable, Long, Pair<Throwable, Long>>) Pair::new)
                                .flatMap((Function<Pair<Throwable, Long>, Publisher<?>>) throwableLongPair -> {
                                    Throwable throwable = throwableLongPair.first;
                                    long time = throwableLongPair.second;
                                    if (throwable instanceof SQLiteFullException) {
                                        if (runnable != null) {
                                            runnable.run();
                                        }
                                        return time == retryConditions.get(retryConditions.size() - 1) ? Flowable.error(throwable) : Flowable.timer(time, TimeUnit.SECONDS);
                                    }
                                    return Flowable.error(throwable);
                                }));
    }

    public static <T> ObservableTransformer<T, T> observableApiRetry(List<Long> retryList) {
        return new ObservableTransformer<T, T>() {
            @Override
            public @NonNull ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Throwable {
                                return throwableObservable
                                        .zipWith(retryList, new BiFunction<Throwable, Long, Pair<Throwable, Long>>() {
                                            @Override
                                            public Pair<Throwable, Long> apply(Throwable throwable, Long aLong) throws Throwable {
                                                return new Pair<>(throwable, aLong);
                                            }
                                        })
                                        .flatMap(new Function<Pair<Throwable, Long>, ObservableSource<Long>>() {
                                            @Override
                                            public ObservableSource<Long> apply(Pair<Throwable, Long> throwableLongPair) throws Throwable {
                                                Throwable throwable = throwableLongPair.first;
                                                long time = throwableLongPair.second;
                                                if (needRetry(throwable)) {
                                                    Timber.tag(TAG).d("Api request try after delay: %s", time);
                                                    return time == retryList.get(retryList.size() - 1) ? Observable.error(new RetryFailException(throwable)) : Observable.timer(time, TimeUnit.SECONDS);
                                                }
                                                return Observable.error(throwable);
                                            }
                                        });
                            }
                        });
            }
        };
    }

    public static <T> ObservableTransformer<ApiResponse<T>, T> observableApiTransformer() {
        return upstream -> upstream.map(tApiResponse -> {
            if (tApiResponse.isSuccess()) {
                return tApiResponse.getData();
            } else {
                throw new ApiException(tApiResponse.getMessage());
            }
        });
    }

    public static <T> FlowableTransformer<ApiResponse<T>, T> flowableApiTransformer() {
        return upstream -> upstream.map(tApiResponse -> {
            if (tApiResponse.isSuccess()) {
                return tApiResponse.getData();
            } else {
                throw new ApiException(tApiResponse.getMessage());
            }
        });
    }

    public static boolean needRetry(Throwable e) {
        if (e instanceof JsonParseException || e instanceof ApiException || e instanceof RetryFailException) {
            return false;
        }
        if (e instanceof NoConnectivityException || e instanceof PingPublicNetException || e instanceof ServerUnReachableException) {
            return false;
        }
        if (e instanceof ConnectException || e instanceof UnknownHostException) {
            return false;
        }
        // TODO add other exception judgement
        return true;
    }

    static class ThrowableWithTime {
        private final Throwable throwable;
        private final int time;

        public ThrowableWithTime(Throwable throwable, int time) {
            this.throwable = throwable;
            this.time = time;
        }

        public Throwable throwable() {
            return throwable;
        }

        public int time() {
            return time;
        }
    }
}
