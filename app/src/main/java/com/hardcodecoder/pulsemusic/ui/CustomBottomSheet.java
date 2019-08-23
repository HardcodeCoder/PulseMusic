package com.hardcodecoder.pulsemusic.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hardcodecoder.pulsemusic.R;

public class CustomBottomSheet extends BottomSheetDialog {

    public CustomBottomSheet(@NonNull Context context) {
        super(context, R.style.BottomSheetDialog);
    }
}
