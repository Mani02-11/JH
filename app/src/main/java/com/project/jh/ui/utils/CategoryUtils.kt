package com.project.jh.ui.utils

import com.project.jh.R

fun getCategoryDrawable(category: String): Int {
    return when (category.trim().lowercase()) {
        "programming" -> R.drawable.programming
        "design" -> R.drawable.design
        "tutoring" -> R.drawable.tutoring
        "photography" -> R.drawable.phototgraphy
        "video editing", "editing" -> R.drawable.editing
        "web dev", "webdev" -> R.drawable.webdev
        else -> R.drawable.others
    }
}
