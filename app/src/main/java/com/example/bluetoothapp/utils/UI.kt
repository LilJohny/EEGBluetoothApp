package com.example.bluetoothapp.utils

import android.app.Activity
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

internal fun Activity.showSnackbarShort(text: CharSequence) {
    Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}

internal fun Activity.showSnackbarShort(@StringRes text: Int) {
    Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}
internal fun Fragment.showSnackbarShort(text: CharSequence) {
    Snackbar.make(this.view!!.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}

internal fun Fragment.showSnackbarShort(@StringRes text: Int) {
    Snackbar.make(this.view!!.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}