package com.example.bluetoothapp.utils

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

internal fun Activity.showToastShort(text: CharSequence) {
    Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}

internal fun Activity.showToastShort(@StringRes text: Int) {
    Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}
internal fun Fragment.showToastShort(text: CharSequence) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

internal fun Fragment.showToastShort(@StringRes text: Int) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}