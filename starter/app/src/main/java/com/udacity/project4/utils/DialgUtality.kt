package com.udacity.project4.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.udacity.project4.R


/**
 * @author Bassem Mohsen : basem3312@gmail.com on 2/7/2023.
 */

fun displayErrorAlertDialog(
    context: Activity,
    message: String,
    isShouldFinishActivity: Boolean
) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(R.string.error_label)
    builder.setMessage(message)

    builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        dialog.dismiss()
        if (isShouldFinishActivity)
            context.finish()
    }


    builder.show()
}

fun displayErrorAlertDialogWthClick(
    context: Activity,
    message: String,
    positiveButtonLabel: String,
    clickListener: DialogInterface.OnClickListener,
) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(R.string.error_label)
    builder.setMessage(message)

    builder.setPositiveButton(positiveButtonLabel, clickListener)



    builder.show()
}