package com.rnlkav.storyapp.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StoryResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("listStory")
    val listStory: List<ListStoryItem>? = null
)

data class ListStoryItem(
    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null,

    @field:SerializedName("lat")
    val lat: Double? = null,

    @field:SerializedName("lon")
    val lon: Double? = null
) : Serializable
