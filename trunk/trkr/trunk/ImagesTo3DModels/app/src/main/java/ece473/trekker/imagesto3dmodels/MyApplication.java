package ece473.trekker.imagesto3dmodels;

import android.app.Application;
import android.content.Context;

/**
 * Created by Ryan Hoefferle on 3/8/2015.
 */
public class MyApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
