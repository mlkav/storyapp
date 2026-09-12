package com.rnlkav.storyapp.ui.login

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
import com.rnlkav.storyapp.data.pref.UserModel
import com.rnlkav.storyapp.databinding.ActivityLoginBinding
import com.rnlkav.storyapp.ui.ViewModelFactory
import com.rnlkav.storyapp.ui.main.MainActivity
import com.rnlkav.storyapp.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAction()
        playAnimation()
        observeViewModel()
    }

    private fun setupAction() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email = edLoginEmail.text.toString()
                val password = edLoginPassword.text.toString()

                val isEmailValid = edLoginEmail.validate()
                val isPasswordValid = edLoginPassword.validate()

                if (isEmailValid && isPasswordValid) {
                    viewModel.login(email, password)
                }
            }

            tvLoginRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModel.loginResponse.observe(this) { response ->
            if (response.error == false) {
                val loginResult = response.loginResult
                if (loginResult != null) {
                    val user = UserModel(
                        email = binding.edLoginEmail.text.toString(),
                        token = loginResult.token ?: "",
                        isLogin = true,
                    )
                    viewModel.saveSession(user)
                    
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playAnimation() {
        val logo = ObjectAnimator.ofFloat(binding.ivLogin, View.ALPHA, 1f).setDuration(500)
        val title = ObjectAnimator.ofFloat(binding.tvLoginTitle, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.tlLoginEmail, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.tlLoginPassword, View.ALPHA, 1f).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val register = ObjectAnimator.ofFloat(binding.tvLoginRegister, View.ALPHA, 1f).setDuration(500)

        binding.ivLogin.alpha = 0f
        binding.tvLoginTitle.alpha = 0f
        binding.tlLoginEmail.alpha = 0f
        binding.tlLoginPassword.alpha = 0f
        binding.btnLogin.alpha = 0f
        binding.tvLoginRegister.alpha = 0f

        AnimatorSet().apply {
            playSequentially(logo, title, email, password, button, register)
            start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}
