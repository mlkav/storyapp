package com.rnlkav.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.data.response.ListStoryItem
import com.rnlkav.storyapp.databinding.ActivityMainBinding
import com.rnlkav.storyapp.ui.ViewModelFactory
import com.rnlkav.storyapp.ui.adapter.LoadingStateAdapter
import com.rnlkav.storyapp.ui.adapter.StoriesAdapter
import com.rnlkav.storyapp.ui.add.AddStoryActivity
import com.rnlkav.storyapp.ui.detail.DetailActivity
import com.rnlkav.storyapp.ui.login.LoginActivity
import com.rnlkav.storyapp.ui.maps.MapsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoriesAdapter
    private var isRefreshingAfterUpload = false

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            isRefreshingAfterUpload = true
            adapter.refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSwipeRefresh()
        observeLoadState()

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                getData()
            }
        }

        binding.apply {
            fabAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
                addStoryLauncher.launch(intent)
            }
        }
    }

    private fun getData() {
        lifecycleScope.launch {
            viewModel.stories.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        adapter = StoriesAdapter { story, ivPhoto ->
            val intent = Intent(this, DetailActivity::class.java)
            val listStoryItem = ListStoryItem(
                id = story.id,
                name = story.name,
                description = story.description,
                photoUrl = story.photoUrl,
                createdAt = story.createdAt,
                lat = story.lat,
                lon = story.lon
            )
            intent.putExtra(DetailActivity.EXTRA_STORY, listStoryItem)
            startActivity(
                intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, ivPhoto, "photo")
                    .toBundle()
            )
        }

        binding.rvStories.apply {
            layoutManager =
                GridLayoutManager(this@MainActivity, resources.getInteger(R.integer.span_count))
            adapter = this@MainActivity.adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    this@MainActivity.adapter.retry()
                }
            )
        }

        this.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.rvStories.smoothScrollToPosition(0)
                }
            }
        })
    }

    private fun observeLoadState() {
        adapter.loadStateFlow.let { loadState ->
            lifecycleScope.launch {
                loadState.collect {
                    val isLoading = it.refresh is androidx.paging.LoadState.Loading
                    showLoading(isLoading)

                    if (it.refresh is androidx.paging.LoadState.NotLoading) {
                        val isListEmpty = adapter.itemCount == 0
                        binding.tvEmpty.visibility = if (isListEmpty) View.VISIBLE else View.GONE

                        if (isRefreshingAfterUpload) {
                            binding.rvStories.scrollToPosition(0)
                            isRefreshingAfterUpload = false
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading && !binding.swipeRefresh.isRefreshing) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }

            R.id.action_logout -> {
                showLogoutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.logout))
            setMessage(getString(R.string.logout_confirm))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.logout()
            }
            setNegativeButton(getString(R.string.no), null)
            show()
        }
    }
}
