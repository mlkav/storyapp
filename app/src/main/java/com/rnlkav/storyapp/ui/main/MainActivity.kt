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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.databinding.ActivityMainBinding
import com.rnlkav.storyapp.ui.ViewModelFactory
import com.rnlkav.storyapp.ui.adapter.StoriesAdapter
import com.rnlkav.storyapp.ui.login.LoginActivity
import com.rnlkav.storyapp.ui.add.AddStoryActivity
import com.rnlkav.storyapp.ui.detail.DetailActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoriesAdapter

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.getStories()
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

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                viewModel.getStories()
            }
        }

        viewModel.listStory.observe(this) { stories ->
            if (stories != null) {
                adapter.submitList(stories)
                binding.tvEmpty.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.isLoading.observe(this) {
            showLoading(it)
        }
        
        binding.apply {
            fabAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
                addStoryLauncher.launch(intent)
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getStories()
        }
    }

    private fun setupRecyclerView() {
        adapter = StoriesAdapter { story, ivPhoto ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY, story)
            startActivity(
                intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, ivPhoto, "photo").toBundle()
            )
        }
        
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.rvStories.smoothScrollToPosition(0)
                }
            }
        })

        binding.rvStories.layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.span_count))
        binding.rvStories.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading && !binding.swipeRefresh.isRefreshing) View.VISIBLE else View.GONE
        if (!isLoading) {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
