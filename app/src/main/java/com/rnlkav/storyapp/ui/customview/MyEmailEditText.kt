package com.rnlkav.storyapp.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.rnlkav.storyapp.R

class MyEmailEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    var isValid: Boolean = false
        private set

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate()

                // Clear TextInputLayout error if it exists
                (parent.parent as? TextInputLayout)?.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun validate(): Boolean {
        val email = text.toString().trim()
        isValid = when {
            email.isEmpty() -> {
                error = context.getString(R.string.error_empty_field)
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                error = context.getString(R.string.error_email_invalid)
                false
            }

            else -> {
                error = null
                true
            }
        }
        return isValid
    }
}
