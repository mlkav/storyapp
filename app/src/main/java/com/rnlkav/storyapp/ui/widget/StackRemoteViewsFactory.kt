package com.rnlkav.storyapp.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.data.Injection
import com.rnlkav.storyapp.data.response.ListStoryItem

class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private var mWidgetItems = ArrayList<ListStoryItem>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val repository = Injection.provideRepository(mContext)
        try {
            val response = kotlinx.coroutines.runBlocking { repository.getStories() }
            if (response.error == false) {
                mWidgetItems.clear()
                mWidgetItems.addAll(response.listStory ?: emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        
        if (mWidgetItems.isNotEmpty()) {
            val story = mWidgetItems[position]
            rv.setTextViewText(R.id.banner_text, story.name)
            
            try {
                val bitmap: Bitmap = Glide.with(mContext)
                    .asBitmap()
                    .load(story.photoUrl)
                    .submit()
                    .get()
                rv.setImageViewBitmap(R.id.imageView, bitmap)
            } catch (e: Exception) {
                rv.setImageViewResource(R.id.imageView, R.drawable.ic_place_holder)
            }
        }

        val extras = Bundle()
        extras.putInt(StoryWidget.EXTRA_ITEM, position)
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}
