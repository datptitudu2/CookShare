package com.example.cookshare.utils;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * Utility class for managing loading states across the app
 * Created for: Nguyễn Tiến Đạt (Lead)
 */
public class LoadingManager {

    public static void showLoading(CircularProgressIndicator progressIndicator, View contentView) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    public static void hideLoading(CircularProgressIndicator progressIndicator, View contentView) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    public static void showLoading(ProgressBar progressBar, View contentView) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    public static void hideLoading(ProgressBar progressBar, View contentView) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    public static void showLoadingWithText(CircularProgressIndicator progressIndicator,
            TextView loadingText,
            View contentView) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        if (loadingText != null) {
            loadingText.setVisibility(View.VISIBLE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    public static void hideLoadingWithText(CircularProgressIndicator progressIndicator,
            TextView loadingText,
            View contentView) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (loadingText != null) {
            loadingText.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }
}
