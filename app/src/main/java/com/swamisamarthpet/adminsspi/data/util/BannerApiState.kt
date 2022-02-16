package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.Banner

sealed class BannerApiState {
    object Empty: BannerApiState()

    class FailureGetAllBanners(val msg:Throwable) : BannerApiState()
    class SuccessGetAllBanners(val data: List<Banner>) : BannerApiState()
    object LoadingGetAllBanners: BannerApiState()

    class FailureInsertBanner(val msg:Throwable) : BannerApiState()
    class SuccessInsertBanner(val data: Int) : BannerApiState()
    object LoadingInsertBanner: BannerApiState()

    class FailureUpdateBanner(val msg:Throwable) : BannerApiState()
    class SuccessUpdateBanner(val data: Int) : BannerApiState()
    object LoadingUpdateBanner: BannerApiState()

    class FailureDeleteBanner(val msg:Throwable) : BannerApiState()
    class SuccessDeleteBanner(val data: Int) : BannerApiState()
    object LoadingDeleteBanner: BannerApiState()

}