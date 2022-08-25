package kr.co.lina.ga.photogallery.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/** 이미지 모델 클래스 */
@Parcelize
data class Item(var imagePath: String, var selected: Int) : Parcelable