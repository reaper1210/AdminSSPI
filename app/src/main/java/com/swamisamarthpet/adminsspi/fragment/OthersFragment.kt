package com.swamisamarthpet.adminsspi.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.adapter.ImageSliderAdapter
import com.swamisamarthpet.adminsspi.adapter.PopularProductAdapter
import com.swamisamarthpet.adminsspi.data.model.Banner
import com.swamisamarthpet.adminsspi.data.model.PopularProduct
import com.swamisamarthpet.adminsspi.data.util.BannerApiState
import com.swamisamarthpet.adminsspi.data.util.PopularProductApiState
import com.swamisamarthpet.adminsspi.databinding.FragmentOthersBinding
import com.swamisamarthpet.adminsspi.ui.BannerViewModel
import com.swamisamarthpet.adminsspi.ui.PopularProductViewModel
import kotlinx.coroutines.flow.collect
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Inflater
import javax.inject.Inject

class OthersFragment : Fragment() {

    private val bannerViewModel: BannerViewModel by activityViewModels()
    private val popularProductViewModel: PopularProductViewModel by activityViewModels()
    private lateinit var binding: FragmentOthersBinding
    private lateinit var bannersList: ArrayList<Banner>
    private lateinit var imagesArrayList: ArrayList<ByteArray>
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    @Inject
    lateinit var popularProductAdapter: PopularProductAdapter
    private var deleteImagePosition: Int = 0
    private var updateImagePosition: Int = 0

    private val insertStartForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        when(resultCode) {
            Activity.RESULT_OK -> {
                val uri = data?.data!!
                bannerViewModel.insertBanner( File(uri.path!!).readBytes())
                handleInsertBannersResponse()
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val updateStartForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        when(resultCode) {
            Activity.RESULT_OK -> {
                val uri = data?.data!!
                bannerViewModel.updateBanner(bannersList[updateImagePosition].bannerId,File(uri.path!!).readBytes())
                handleUpdateBannersResponse()
                imageSliderAdapter.notifyDataSetChanged()
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOthersBinding.inflate(inflater,container,false)

        popularProductAdapter = PopularProductAdapter()
        popularProductViewModel.getAllPopularProducts()
        handleGetAllPopularProductsResponse()
        bannerViewModel.getAllBanners()
        handleGetAllBannersResponse()

        binding.apply{

            btnAddBannerOthersFragment.setOnClickListener {
                ImagePicker.with(requireActivity())
                    .crop(3f,2f)
                    .compress(512)
                    .maxResultSize(512,512)
                    .createIntent { intent ->
                        insertStartForSliderImageResult.launch(intent)
                    }
            }

            btnRemoveBannerOthersFragment.setOnClickListener {
                deleteImagePosition = bannersOthersFragment.currentPagePosition
                bannerViewModel.deleteBanner(bannersList[bannersOthersFragment.currentPagePosition].bannerId)
                handleDeleteBannersResponse()
            }

            bannersOthersFragment.setOnClickListener {
                updateImagePosition = bannersOthersFragment.currentPagePosition
                ImagePicker.with(requireActivity())
                    .crop(3f,2f)
                    .compress(512)
                    .maxResultSize(512,512)
                    .createIntent { intent ->
                        updateStartForSliderImageResult.launch(intent)
                    }
            }

        }

        return binding.root
    }

    private fun handleGetAllPopularProductsResponse(){
        lifecycleScope.launchWhenStarted {
            popularProductViewModel.popularProductApiStateFlow.collect {
                when (it) {
                    is PopularProductApiState.LoadingGetAllPopularProducts -> {
                        binding.progressBarLayoutPopularProductsOthersFragment.visibility = View.VISIBLE
                    }
                    is PopularProductApiState.SuccessGetAllPopularProducts -> {
                        binding.apply {
                            if(it.data.isNullOrEmpty()){
                                txtNoPopularProductOthersFragment.visibility = View.VISIBLE
                                recyclerViewPopularProducts.visibility = View.GONE
                            }
                            recyclerViewPopularProducts.apply{
                                adapter = popularProductAdapter
                                layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                            }
                            progressBarLayoutPopularProductsOthersFragment.visibility = View.GONE
                            popularProductAdapter.submitList(it.data)
                        }
                    }
                    is PopularProductApiState.FailureGetAllPopularProducts -> {
                        binding.progressBarLayoutPopularProductsOthersFragment.visibility = View.GONE
                    }
                    is PopularProductApiState.Empty -> {
                        binding.progressBarLayoutPopularProductsOthersFragment.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleGetAllBannersResponse(){
        lifecycleScope.launchWhenStarted {
            bannerViewModel.bannerApiStateFlow.collect{ bannerApiState ->
                when (bannerApiState) {
                    is BannerApiState.LoadingGetAllBanners -> {
                        binding.apply{
                            bannersProgressBarLayoutOthersFragment.visibility = View.VISIBLE
                            btnAddBannerOthersFragment.isClickable = false
                            btnRemoveBannerOthersFragment.isClickable = false
                        }
                    }
                    is BannerApiState.SuccessGetAllBanners -> {
                        bannersList = bannerApiState.data as ArrayList<Banner>
                        val bannerImageList = ArrayList<String>()
                        bannerApiState.data.forEach { banner->
                            bannerImageList.add(banner.bannerImage)
                        }
                        imagesArrayList = getAllBannersDecompressed(bannerImageList)
                        imageSliderAdapter = ImageSliderAdapter(imagesArrayList,requireActivity())
                        binding.apply {
                            bannersOthersFragment.apply{
                                setSliderAdapter(imageSliderAdapter)
                                setIndicatorAnimation(IndicatorAnimationType.WORM)
                                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
                            }
                            bannersProgressBarLayoutOthersFragment.visibility = View.GONE
                            btnAddBannerOthersFragment.isClickable = true
                            btnRemoveBannerOthersFragment.isClickable = true
                        }
                    }
                    is BannerApiState.FailureGetAllBanners -> {
                        Toast.makeText(requireActivity(),"Some Error occurred while loading banners.",
                            Toast.LENGTH_SHORT).show()
                        binding.apply{
                            bannersProgressBarLayoutOthersFragment.visibility = View.GONE
                            btnAddBannerOthersFragment.isClickable = true
                            btnRemoveBannerOthersFragment.isClickable = true
                        }
                    }
                    else ->{}
                }
            }
        }
    }

    private fun handleInsertBannersResponse() {
        lifecycleScope.launchWhenStarted {
            bannerViewModel.bannerApiStateFlow.collect{ bannerApiState ->
                when (bannerApiState) {
                    is BannerApiState.LoadingInsertBanner -> {
                        binding.btnAddBannerOthersFragment.isClickable = false
                    }
                    is BannerApiState.SuccessInsertBanner -> {
                        if(bannerApiState.data==1){
                            bannerViewModel.getAllBanners()
                            binding.btnAddBannerOthersFragment.isClickable = true
                        }
                    }
                    is BannerApiState.FailureInsertBanner -> {
                        Toast.makeText(requireActivity(),"Some Error occurred while Adding Banner.", Toast.LENGTH_SHORT).show()
                        binding.btnAddBannerOthersFragment.isClickable = true
                    }
                    else ->{}
                }
            }
        }
    }

    private fun handleUpdateBannersResponse() {
        lifecycleScope.launchWhenStarted {
            bannerViewModel.bannerApiStateFlow.collect{ bannerApiState ->
                when (bannerApiState) {
                    is BannerApiState.LoadingUpdateBanner -> {
                        binding.btnAddBannerOthersFragment.isClickable = false
                    }
                    is BannerApiState.SuccessUpdateBanner -> {
                        if(bannerApiState.data==1){
                            bannerViewModel.getAllBanners()
                            binding.btnAddBannerOthersFragment.isClickable = true
                        }
                    }
                    is BannerApiState.FailureUpdateBanner -> {
                        Toast.makeText(requireActivity(),"Some Error occurred while Adding Banner.", Toast.LENGTH_SHORT).show()
                        binding.btnAddBannerOthersFragment.isClickable = true
                    }
                    else ->{}
                }
            }
        }
    }

    private fun handleDeleteBannersResponse() {
        lifecycleScope.launchWhenStarted {
            bannerViewModel.bannerApiStateFlow.collect{ bannerApiState ->
                when (bannerApiState) {
                    is BannerApiState.LoadingDeleteBanner -> {
                        binding.btnRemoveBannerOthersFragment.isClickable = false
                    }
                    is BannerApiState.SuccessDeleteBanner -> {
                        if(bannerApiState.data==1){
                            bannerViewModel.getAllBanners()
                            binding.btnRemoveBannerOthersFragment.isClickable = true
                        }
                    }
                    is BannerApiState.FailureDeleteBanner -> {
                        Toast.makeText(requireActivity(),"Some Error occurred while Removing Banner.", Toast.LENGTH_SHORT).show()
                        binding.btnRemoveBannerOthersFragment.isClickable = true
                    }
                    else ->{}
                }
            }
        }
    }

    private fun getAllBannersDecompressed(list: List<String>): ArrayList<ByteArray> {
        val bannersDecompressedList = ArrayList<ByteArray>()
        for(bannerString in list){
            val decompressor = Inflater()
            val stringArray = bannerString.replace("[","").replace("]","").replace(" ","").split(",")
            val bytes = ByteArray(bannerString.length)
            for(i in 0..stringArray.lastIndex){
                val byte = stringArray[i].toByte()
                bytes[i] = byte
            }
            decompressor.setInput(bytes)
            val bos = ByteArrayOutputStream(bytes.size)
            val buf = ByteArray(1024)
            while (!decompressor.finished()) {
                val count = decompressor.inflate(buf)
                bos.write(buf, 0, count)
            }
            bos.close()
            bannersDecompressedList.add(bos.toByteArray())
        }
        return bannersDecompressedList
    }

}