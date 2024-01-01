package ipt.lei.dam.ncrapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val sortOption = MutableLiveData<String>()

    fun setSortOption(sortOption: String) {
        this.sortOption.value = sortOption
    }
}