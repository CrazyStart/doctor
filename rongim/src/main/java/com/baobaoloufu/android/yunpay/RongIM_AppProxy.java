package com.baobaoloufu.android.yunpay;

import android.app.Application;
import android.widget.Toast;

import java.util.List;

import io.dcloud.weex.AppHookProxy;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.MainActivity;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class RongIM_AppProxy implements AppHookProxy {
    @Override
    public void onCreate(Application application) {
        // k51hidwqk48jb   tdrvipksty015
        RongIM.init(application, BuildConfig.RONG_KEY);

        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if(moduleList != null){
            for(IExtensionModule module : moduleList){
                defaultModule = module;
                break;
            }
            if(defaultModule != null){
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
                RongExtensionManager.getInstance().registerExtensionModule(new SampleExtensionModule());
            }
        }

        if (0 != SealNotificationReceiver.count) {
            //角标清空
            SealNotificationReceiver.count = 0;
            AppShortCutUtil.setCount(SealNotificationReceiver.count, application.getApplicationContext());
        }

    }
}
