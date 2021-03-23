package com.rafo.pixabay.api.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchHit(
    val id: String,
    val largeImageURL: String,
    val webformatURL: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val user: String,
    val userImageURL: String,
    val downloads: Long,
    val imageSize: String,
    val views: Long,
) : Parcelable