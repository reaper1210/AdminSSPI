package com.swamisamarthpet.adminsspi.data.repository

import com.swamisamarthpet.adminsspi.data.model.Banner
import com.swamisamarthpet.adminsspi.data.network.BannerApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class BannerRepository
@Inject
constructor(private val bannerApiService: BannerApiService) {

    fun getAllBanners(): Flow<List<Banner>> = flow {
        emit(bannerApiService.getAllBanners())
    }.flowOn(Dispatchers.IO)

    fun insertBanner(bannerImage: ByteArray): Flow<Int> = flow {
        emit(bannerApiService.insertBanner(bannerImage))
    }.flowOn(Dispatchers.IO)

    fun updateBanner(bannerId: Int, bannerImage: ByteArray): Flow<Int> = flow {
        emit(bannerApiService.updateBanner(bannerId, bannerImage))
    }.flowOn(Dispatchers.IO)

    fun deleteBanner(bannerId: Int): Flow<Int> = flow {
        emit(bannerApiService.deleteBanner(bannerId))
    }.flowOn(Dispatchers.IO)

}