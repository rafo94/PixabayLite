package com.rafo.pixabay.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.rafo.pixabay.BaseFragment
import com.rafo.pixabay.api.data.SearchHit
import com.rafo.pixabay.databinding.DetailFragmentBinding
import com.rafo.pixabay.ui.dialog.InternetConBottomSheet
import com.rafo.pixabay.ui.dialog.InternetConBottomSheet.Companion.INTERNET_CON
import com.rafo.pixabay.util.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailFragment : BaseFragment() {

    private var binding: DetailFragmentBinding? = null
    private var searchHit: SearchHit? = null
    private var imageUrl = ""
    private var showed = true
    private var bottomSheetOpened = false

    private lateinit var connectionLiveData: ConnectionLiveData

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                showDirectoryPathDialog(requireContext(), imageUrl, { length, downloaded ->
                    binding?.run {
                        downloadPb.visible()
                        downloadPb.max = length
                        downloadPb.progress = downloaded
                    }
                }) {
                    binding?.run { downloadPb.gone() }
                }
            else {
                Toast.makeText(
                    requireContext(),
                    "permission is disabled, please enable it for save image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        connectionLiveData = ConnectionLiveData(requireContext())
        val binding = DetailFragmentBinding.inflate(inflater, container, false)
        this.binding = binding
        arguments?.run {
            searchHit = getParcelable(PIXABY_ITEM)
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.run {
            searchHit?.run {
                largeImage.load(largeImageURL)
                userName.text = user
                viewsTv.text = "$views views"
                downloadTv.text = "$downloads download"
                profileImage.load(userImageURL)
                setupImage.setOnClickListener {
                    largeImageURL.setupWallpaper(requireContext())
                    Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
                }
                downloadImage.setOnClickListener {
                    imageUrl = largeImageURL
                    setPermissions()
                }
            }
            largeImage.setOnClickListener {
                showed = if (showed) {
                    topContainer.slideTopUp()
                    bottomContainer.slideBottomDown()
                    false
                } else {
                    topContainer.slideTopDown()
                    bottomContainer.slideBottomUp()
                    true
                }
            }
        }
        val bottomSheet = InternetConBottomSheet.newInstance()
        connectionLiveData.observe(viewLifecycleOwner, { isConnected ->
            if (isConnected) {
                if (bottomSheetOpened) {
                    bottomSheet.dismiss()
                    bottomSheetOpened = false
                }
            } else {
                bottomSheetOpened = true
                bottomSheet.show(childFragmentManager, INTERNET_CON)
            }
        })
    }

    private fun setPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> {
                    val showRequestPermission =
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (showRequestPermission) {
                        registerForActivityResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        showDirectoryPathDialog(requireContext(), imageUrl, { length, downloaded ->
                            binding?.run {
                                downloadPb.visible()
                                downloadPb.max = length
                                downloadPb.progress = downloaded
                            }
                        }) {
                            binding?.run { downloadPb.gone() }
                        }
                    }
                }
                else -> {
                    registerForActivityResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        } else {
            showDirectoryPathDialog(requireContext(), imageUrl, { length, downloaded ->
                binding?.run {
                    downloadPb.visible()
                    downloadPb.max = length
                    downloadPb.progress = downloaded
                }
            }) {
                binding?.run { downloadPb.gone() }
            }
        }
    }

    companion object {
        const val PIXABY_ITEM = "pixaby_item"
    }
}