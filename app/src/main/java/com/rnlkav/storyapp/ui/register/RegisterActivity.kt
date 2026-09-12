package com.rnlkav.storyapp.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.databinding.ActivityRegisterBinding
import com.rnlkav.storyapp.ui.ViewModelFactory
import com.rnlkav.storyapp.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAction()
        playAnimation()
        observeViewModel()
        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.edRegisterName.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.tlRegisterName.error = null
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            },
        )
    }

    private fun setupAction() {
        binding.apply {
            btnRegister.setOnClickListener {
                val name = edRegisterName.text.toString()
                val email = edRegisterEmail.text.toString()
                val password = edRegisterPassword.text.toString()

                val isEmailValid = edRegisterEmail.validate()
                val isPasswordValid = edRegisterPassword.validate()

                when {
                    name.isEmpty() -> {
                        tlRegisterName.error = getString(R.string.error_empty_field)
                    }

                    isEmailValid && isPasswordValid -> {
                        viewModel.register(name, email, password)
                    }
                }
            }

            tvRegisterLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModel.registerResponse.observe(this) { response ->
            if (response.error == false) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playAnimation() {
        val logo = ObjectAnimator.ofFloat(binding.ivRegister, View.ALPHA, 1f).setDuration(500)
        val title = ObjectAnimator.ofFloat(binding.tvRegisterTitle, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.tlRegisterName, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.tlRegisterEmail, View.ALPHA, 1f).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(binding.tlRegisterPassword, View.ALPHA, 1f).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.tvRegisterLogin, View.ALPHA, 1f).setDuration(500)

        binding.ivRegister.alpha = 0f
        binding.tvRegisterTitle.alpha = 0f
        binding.tlRegisterName.alpha = 0f
        binding.tlRegisterEmail.alpha = 0f
        binding.tlRegisterPassword.alpha = 0f
        binding.btnRegister.alpha = 0f
        binding.tvRegisterLogin.alpha = 0f

        AnimatorSet().apply {
            playSequentially(logo, title, name, email, password, button, login)
            start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }
}
