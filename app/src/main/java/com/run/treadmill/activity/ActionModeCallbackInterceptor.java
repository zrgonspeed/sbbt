package com.run.treadmill.activity;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 来源--->https://blog.csdn.net/zrbcsdn/article/details/77556333
 */
public class ActionModeCallbackInterceptor implements ActionMode.Callback {


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }


    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }
}
