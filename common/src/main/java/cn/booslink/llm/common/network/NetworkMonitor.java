package cn.booslink.llm.common.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

import java.lang.ref.WeakReference;

import cn.booslink.llm.common.model.ConnectType;
import cn.booslink.llm.common.model.NetworkStatus;
import cn.booslink.llm.common.utils.NetworkUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import timber.log.Timber;

public class NetworkMonitor {

    private static final String TAG = "NetworkMonitor";
    private static final int NETWORK_FAIL_COUNT = 3;

    private final Context mContext;
    private final WeakReference<ConnectivityManager> mConnectivityRef;
    private final BehaviorSubject<NetworkStatus> mNetworkPublish;
    private ConnectType mConnectType;
    private volatile int mContinueNetworkFailCount = 0;

    public NetworkMonitor(Context context) {
        this.mContext = context;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mConnectivityRef = new WeakReference<>(connectivityManager);
        boolean isConnected = NetworkUtils.isConnected(context);
        mNetworkPublish = BehaviorSubject.createDefault(isConnected ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
        registerNetworkCallback(context);
    }

    @WorkerThread
    public synchronized void requestFailureAndAfterCheckConnectivity(NetworkStatus networkStatus) {
        if (mContinueNetworkFailCount < NETWORK_FAIL_COUNT) {
            mContinueNetworkFailCount++;
            return;
        }
        mNetworkPublish.onNext(networkStatus);
    }

    public void requestSuccess() {
        mContinueNetworkFailCount = 0;
    }

    public Observable<NetworkStatus> getNetworkObservable() {
        return mNetworkPublish;
    }

    public ConnectType getConnectType() {
        if (mConnectType == null || mConnectType == ConnectType.NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mConnectivityRef.get() != null) {
                NetworkCapabilities capabilities = mConnectivityRef.get().getNetworkCapabilities(mConnectivityRef.get().getActiveNetwork());
                if (capabilities != null) {
                    boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    boolean isConnected = NetworkUtils.isConnected(mContext);
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && hasInternet && isConnected) {
                        mConnectType = ConnectType.WIFI;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && hasInternet && isConnected) {
                        mConnectType = ConnectType.MOBILE;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) && hasInternet && isConnected) {
                        mConnectType = ConnectType.ETHERNET;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && hasInternet && isConnected) {
                        mConnectType = ConnectType.VPN;
                    } else {
                        mConnectType = hasInternet && isConnected ? ConnectType.OTHER : ConnectType.NONE;
                    }
                }
            } else {
                mConnectType = getConnectTypeByContext(mContext);
            }
        }
        return mConnectType == null ? ConnectType.NONE : mConnectType;
    }

    private void registerNetworkCallback(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mConnectivityRef.get() != null) {
            mConnectivityRef.get().registerDefaultNetworkCallback(new DefaultNetworkCallback());
        } else {
            context.registerReceiver(new DefaultNetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private ConnectType getConnectTypeByContext(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivity.getAllNetworkInfo();
        boolean isConnect = NetworkUtils.isConnected(mContext);
        if (!isConnect) return ConnectType.NONE;
        for (NetworkInfo info : networkInfo) {
            if (info.getState() != NetworkInfo.State.CONNECTED)
                continue;
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return ConnectType.WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return ConnectType.MOBILE;
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return ConnectType.ETHERNET;
            } else if (info.getType() == ConnectivityManager.TYPE_VPN) {
                return ConnectType.VPN;
            }
        }
        return ConnectType.NONE;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private class DefaultNetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Timber.tag(TAG).d("onLost");
            mConnectType = ConnectType.NONE;
            mNetworkPublish.onNext(NetworkStatus.DISCONNECTED);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            NetworkStatus networkStatus = mConnectType != ConnectType.NONE ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED;
            boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean isConnected = NetworkUtils.isConnected(mContext);
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && hasInternet && isConnected) {
                mConnectType = ConnectType.WIFI;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && hasInternet && isConnected) {
                mConnectType = ConnectType.MOBILE;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) && hasInternet && isConnected) {
                mConnectType = ConnectType.ETHERNET;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && hasInternet && isConnected) {
                mConnectType = ConnectType.VPN;
            } else {
                mConnectType = hasInternet && isConnected ? ConnectType.OTHER : ConnectType.NONE;
            }
            NetworkStatus newNetworkStatus = mConnectType != ConnectType.NONE ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED;
            if (networkStatus != newNetworkStatus) {
                Timber.tag(TAG).d("onCapabilitiesChanged network changed: %s", mConnectType);
                mNetworkPublish.onNext(newNetworkStatus);
            }
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Timber.tag(TAG).d("onAvailable");
            mConnectType = getConnectType();
            mNetworkPublish.onNext(mConnectType != ConnectType.NONE ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            Timber.tag(TAG).d("onAvailable");
            mConnectType = getConnectType();
            mNetworkPublish.onNext(mConnectType != ConnectType.NONE ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
        }
    }

    private class DefaultNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkStatus networkStatus = mConnectType != ConnectType.NONE ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED;
            mConnectType = getConnectTypeByContext(context);
            NetworkStatus newNetworkStatus = mConnectType != ConnectType.NONE ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED;
            if (networkStatus != newNetworkStatus) {
                mNetworkPublish.onNext(newNetworkStatus);
                Timber.tag(TAG).d("onReceive: %s, connect type: %s", intent.getAction(), mConnectType);
            }
        }
    }
}
