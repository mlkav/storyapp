package com.rnlkav.storyapp.utils

import com.rnlkav.storyapp.data.database.StoryEntity

object DataDummy {

    fun generateDummyStoryEntity(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                i.toString(),
                "author + $i",
                "quote $i",
                "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png",
                "2022-01-08T06:34:18.598Z",
                -10.212,
                -16.002
            )
            items.add(story)
        }
        return items
    }
}
