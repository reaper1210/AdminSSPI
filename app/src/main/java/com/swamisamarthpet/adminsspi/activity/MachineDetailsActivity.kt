package com.swamisamarthpet.adminsspi.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.adapter.ImageSliderAdapter
import com.swamisamarthpet.adminsspi.adapter.MachineDetailsAdapter
import com.swamisamarthpet.adminsspi.adapter.PartsAdapter
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.data.model.Machine
import com.swamisamarthpet.adminsspi.data.util.MachineApiState
import com.swamisamarthpet.adminsspi.data.util.PartApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityMachineDetailsBinding
import com.swamisamarthpet.adminsspi.ui.MachineViewModel
import com.swamisamarthpet.adminsspi.ui.PartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.Inflater
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class MachineDetailsActivity : AppCompatActivity() {

    private val partViewModel: PartViewModel by viewModels()
    private val machineViewModel: MachineViewModel by viewModels()

    @Inject
    lateinit var machineDetailsAdapter: MachineDetailsAdapter

    @Inject
    lateinit var partsAdapter: PartsAdapter
    private lateinit var binding: ActivityMachineDetailsBinding
    private lateinit var machine: Machine
    private var parts: List<HashMap<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineDetailsBinding.inflate(layoutInflater)

        binding.txtNoSpareParts.visibility = View.GONE
        machineDetailsAdapter = MachineDetailsAdapter()
        partsAdapter = PartsAdapter()
        val machineId = intent.getIntExtra("machineId", 0)
        if (Constants.currentCategory?.categoryName != null) {
            machineViewModel.getMachineById(machineId, Constants.currentCategory!!.categoryName)
            handleMachineDetailsResponse()
        }

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (binding.pdfViewMachineDetailsAct.visibility == View.VISIBLE) {
            binding.pdfViewMachineDetailsAct.visibility = View.GONE
            binding.topLayoutChipMachineDetailsActivity.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun handleMachineDetailsResponse() {
        lifecycleScope.launchWhenStarted {
            machineViewModel.machineApiStateFlow.collect { machineApiState ->
                when (machineApiState) {
                    is MachineApiState.LoadingGetMachineById -> {
                        binding.apply{
                            machineDetailsActProgressBarLayout.visibility = View.VISIBLE
                        }
                    }
                    is MachineApiState.SuccessGetMachineById -> {
                        machine = machineApiState.data
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                        partViewModel.getAllParts(machine.machineName)
                        handlePartsResponse()
                        setValues()
                    }
                    is MachineApiState.FailureGetMachineById -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    is MachineApiState.EmptyGetMachineById -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {}
                }
            }

        }
    }

    private fun handlePartsResponse() {
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState ->
                when (partApiState) {
                    is PartApiState.LoadingGetAllParts -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.VISIBLE
                    }
                    is PartApiState.SuccessGetAllParts -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        parts = partApiState.data
                        if (parts.isEmpty()) {
                            binding.txtNoSpareParts.visibility = View.VISIBLE
                        }
                        setPartRecycler()
                    }
                    is PartApiState.FailureGetAllParts -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        binding.txtNoSpareParts.visibility = View.VISIBLE
                    }
                    is PartApiState.Empty -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        binding.txtNoSpareParts.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        binding.txtNoSpareParts.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setValues() {

        val machineDetails = machine.machineDetails.split(";")
        val detailsList = ArrayList<Details>()
        for (detail in machineDetails) {
            val keyValue = detail.split(":")
            detailsList.add(Details(keyValue[0], keyValue[1]))
        }
        machineDetailsAdapter.submitList(detailsList)

        val imagesArrayList = ArrayList<Any>()
        val imagesStringList = machine.machineImages.split(";")
        for (imageString in imagesStringList) {
            val decompressor = Inflater()
            val stringArray =
                imageString.replace("[", "").replace("]", "").replace(" ", "").split(",")
            val bytes = ByteArray(imageString.length)
            for (i in 0..stringArray.lastIndex) {
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
            val decompressedImageByteArray = bos.toByteArray()
            imagesArrayList.add(decompressedImageByteArray)
        }
        if(getVideoIdFromUrl(machine.youtubeVideoLink)!=null){
            imagesArrayList.add(getVideoIdFromUrl(machine.youtubeVideoLink).toString())
        }
        val imageSliderAdapter = ImageSliderAdapter(imagesArrayList, this@MachineDetailsActivity)

        binding.apply {
            txtProductNameMachineDetails.text = machine.machineName
            imageMachineDetailsAct.apply {
                setSliderAdapter(imageSliderAdapter)
                setIndicatorAnimation(IndicatorAnimationType.WORM)
                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            }
            btnInterestedMachineDetailsAct.setOnClickListener {
                val intent = Intent(this@MachineDetailsActivity, MainActivity::class.java)
                intent.putExtra("supportFragmentRedirect", true)
                intent.putExtra("productName", machine.machineName)
                startActivity(intent)
            }

            machineDetailsRecycler.apply {
                layoutManager = LinearLayoutManager(this@MachineDetailsActivity)
                adapter = machineDetailsAdapter
            }

            if (machine.machinePdf == "") {
                btnViewPdf.visibility = View.INVISIBLE
            }

            btnViewPdf.setOnClickListener {
                binding.apply {
                    pdfViewMachineDetailsAct.fromBytes(getPdfDecompressedByteArray(machine.machinePdf))
                        .onError {
                            println("Pdf Error: ${it.message}")
                        }
                        .enableSwipe(true)
                        .load()

                    pdfViewMachineDetailsAct.visibility = View.VISIBLE
                    topLayoutChipMachineDetailsActivity.visibility = View.VISIBLE
                }
            }

            topLayoutChipMachineDetailsActivity.setOnClickListener {
                pdfViewMachineDetailsAct.visibility = View.GONE
                topLayoutChipMachineDetailsActivity.visibility = View.GONE
            }

            btnBackMachineDetailsActivity.setOnClickListener {
                onBackPressed()
            }
        }

    }

    private fun setPartRecycler() {
        partsAdapter.submitList(parts)
        binding.apply {
            partsRecycler.apply {
                layoutManager = LinearLayoutManager(
                    this@MachineDetailsActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = partsAdapter
            }
        }
    }

    private fun getPdfDecompressedByteArray(pdfString: String): ByteArray {
        val decompressor = Inflater()
        val stringArray = pdfString.replace("[", "").replace("]", "").replace(" ", "").split(",")
        val bytes = ByteArray(pdfString.length)
        for (i in 0..stringArray.lastIndex) {
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
        return bos.toByteArray()
    }

    private fun getVideoIdFromUrl(url: String): String? {
        val expression = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        if (url.trim().isEmpty()) {
            return null
        }
        val pattern = Pattern.compile(expression)
        val matcher = pattern.matcher(url)
        try {
            if (matcher.find())
                return matcher.group()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return null
    }
}
