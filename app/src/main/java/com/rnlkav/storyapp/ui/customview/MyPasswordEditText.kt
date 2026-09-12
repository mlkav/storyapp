package com.rnlkav.storyapp.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.rnlkav.storyapp.R

class MyPasswordEditText @JvmOverloads constructor(
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
        val password = text.toString()
        isValid = when {
            password.isEmpty() -> {
                setError(context.getString(R.string.error_empty_field), null)
                false
            }
            password.length < 8 -> {
                setError(context.getString(R.string.error_password_short), null)
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
