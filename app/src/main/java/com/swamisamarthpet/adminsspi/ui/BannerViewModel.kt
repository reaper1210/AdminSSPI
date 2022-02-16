package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.BannerRepository
import com.swamisamarthpet.adminsspi.data.util.BannerApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BannerViewModel
@Inject constructor(private val bannerRepository: BannerRepository): ViewModel() {

    private val _Banner_apiStateFlow: MutableStateFlow<BannerApiState> = MutableStateFlow(BannerApiState.Empty)
    val bannerApiStateFlow: StateFlow<BannerApiState> = _Banner_apiStateFlow

    fun getAllBanners() = viewModelScope.launch {
        bannerRepository.getAllBanners()
            .onStart {
                _Banner_apiStateFlow.value = BannerApiState.LoadingGetAllBanners
            }
            .catch { e ->
                _Banner_apiStateFlow.value = BannerApiState.FailureGetAllBanners(e)
            }
            .collect {response->
                _Banner_apiStateFlow.value = BannerApiState.SuccessGetAllBanners(response)
            }
    }

    fun insertBanner(bannerImage: ByteArray) = viewModelScope.launch {
        bannerRepository.insertBanner(bannerImage)
            .onStart {
                _Banner_apiStateFlow.value = BannerApiState.LoadingInsertBanner
            }
            .catch { e ->
                _Banner_apiStateFlow.value = BannerApiState.FailureInsertBanner(e)
            }
            .collect {response->
                _Banner_apiStateFlow.value = BannerApiState.SuccessInsertBanner(response)
            }
    }

    fun updateBanner(bannerId: Int, bannerImage: ByteArray) = viewModelScope.launch {
        bannerRepository.updateBanner(bannerId, bannerImage)
            .onStart {
                _Banner_apiStateFlow.value = BannerApiState.LoadingUpdateBanner
            }
            .catch { e ->
                _Banner_apiStateFlow.value = BannerApiState.FailureUpdateBanner(e)
            }
            .collect { response ->
                _Banner_apiStateFlow.value = BannerApiState.SuccessUpdateBanner(response)
            }
    }

    fun deleteBanner(bannerId: Int) = viewModelScope.launch {
        bannerRepository.deleteBanner(bannerId)
            .onStart {
                _Banner_apiStateFlow.value = BannerApiState.LoadingDeleteBanner
            }
            .catch { e ->
                _Banner_apiStateFlow.value = BannerApiState.FailureDeleteBanner(e)
            }
            .collect { response->
                _Banner_apiStateFlow.value = BannerApiState.SuccessDeleteBanner(response)
            }
    }

}