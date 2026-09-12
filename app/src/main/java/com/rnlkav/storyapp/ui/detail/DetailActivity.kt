package com.rnlkav.storyapp.ui.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.rnlkav.storyapp.data.response.ListStoryItem
import com.rnlkav.storyapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val story = intent.getSerializableExtra(EXTRA_STORY) as? ListStoryItem
        if (story != null) {
            setupDetail(story)
        }
    }

    private fun setupDetail(story: ListStoryItem) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}
