package com.example.uncutgyms.ui.main.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(msg: String?) {
    if (msg != null) {
        Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
            .setAction("dismiss") {}
            .show()
    }
}

fun ImageRequest.Builder.roundedCorners() {
    crossfade(true)
    transformations(
        RoundedCornersTransformation(16f)
    )
    fallback(com.google.android.material.R.color.m3_ref_palette_dynamic_neutral70)
}

fun Fragment.requestLocationPermission(
    userPermanentlyDeniedLocPermissions: Boolean,
    onGrantedCallback: () -> Unit,
    askConfirmationToSkip: () -> Unit,
    onUserPermanentlyDenied: () -> Unit
): ActivityResultLauncher<String> {
    return registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        when {
            isGranted -> {
                onGrantedCallback()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                askConfirmationToSkip()
            }
            else -> {
                if (!userPermanentlyDeniedLocPermissions) {
                    onUserPermanentlyDenied()
                    askConfirmationToSkip()
                } else {
                    requireContext().openAppSettings()
                }
            }
        }
    }
}

fun Context.openAppSettings() =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(this)
    }