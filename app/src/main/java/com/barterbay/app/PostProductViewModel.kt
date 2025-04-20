package com.barterbay.app

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.airbnb.lottie.LottieAnimationView

class PostProductViewModel : ViewModel() {
    val name = MutableLiveData<String>()
    val lottieInNameFragment = MutableLiveData<String>()
    val categoryLottieName = MutableLiveData<String>()
    //    val images = MutableLiveData<List<Uri>>)
    val images = MutableLiveData<List<Uri>>()
    val selectedCategory = MutableLiveData<CategoryModel?>()
    val price = MutableLiveData<Double>()
    val summary = MutableLiveData<String>()

//    fun reset() {
//        category.value = ""
////        images.value = emptyList()
//        images.value = ""
//        price.value = ""
//        summary.value = ""
//    }
}
