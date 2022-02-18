package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.PopularProduct

sealed class PopularProductApiState{
    object Empty: PopularProductApiState()
    class FailureGetAllPopularProducts(val msg:Throwable) : PopularProductApiState()
    class SuccessGetAllPopularProducts(val data: List<HashMap<String,String>>) : PopularProductApiState()
    object LoadingGetAllPopularProducts : PopularProductApiState()

    class FailureGetPopularProductById(val msg:Throwable) : PopularProductApiState()
    class SuccessGetPopularProductById(val data: PopularProduct): PopularProductApiState()
    object LoadingGetPopularProductById : PopularProductApiState()

    class FailureUpdatePopularProduct(val msg:Throwable) : PopularProductApiState()
    class SuccessUpdatePopularProduct(val data: Int): PopularProductApiState()
    object LoadingUpdatePopularProduct : PopularProductApiState()

    class FailureDeletePopularProduct(val msg:Throwable) : PopularProductApiState()
    class SuccessDeletePopularProduct(val data: Int): PopularProductApiState()
    object LoadingDeletePopularProduct : PopularProductApiState()

}
