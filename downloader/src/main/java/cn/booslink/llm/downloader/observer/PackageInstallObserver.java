package cn.booslink.llm.downloader.observer;

import android.content.pm.IPackageInstallObserver;
import android.os.RemoteException;

public class PackageInstallObserver extends IPackageInstallObserver.Stub {

    @Override
    public void packageInstalled(String packageName, int returnCode) throws RemoteException {

    }
}
