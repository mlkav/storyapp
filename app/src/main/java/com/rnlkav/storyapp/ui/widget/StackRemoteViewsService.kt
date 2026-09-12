package com.rnlkav.storyapp.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService

class StackRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}
