package com.barterbay.app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.barterbay.app.databinding.FragmentImagesBinding
import com.bumptech.glide.Glide
import android.Manifest

class ImagesFragment : Fragment() {
    private lateinit var binding: FragmentImagesBinding
    private lateinit var viewModel: PostProductViewModel
    private var selectedImages = mutableListOf<Uri>()
    private var ifSingleImageSelected : Boolean = false

    // Register for activity result
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedImages.clear() // Clear previous selections

                // Handle multiple image selection
                if (data.clipData != null) {
                    val clipData = data.clipData!!
                    val count = minOf(clipData.itemCount, 6) // Get up to 6 images
                    for (i in 0 until count) {
                        selectedImages.add(clipData.getItemAt(i).uri)
                    }
                }
                // Handle single image selection
                else if (data.data != null) {
                    data.data?.let { uri ->
                        selectedImages.add(uri)
                    }
                }
                ifSingleImageSelected = false
                updateImageViews()
            }
        }
    }


    // Add this with your other properties
    private val singleImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Update only the clicked image position
                val position = getClickedImagePosition()
                if (position != -1) {
                    if (selectedImages.size > position) {   // position should be less less (as it starts from 0) than the size of list, it will update the desired element
                        selectedImages[position] = uri
                    } else {    // this else part is not that necessary to look upon, it will never execute technically.
                        // If position is beyond current size, add nulls until we reach the position
                        while (selectedImages.size <= position) {
                            selectedImages.add(Uri.EMPTY)
                        }
                        selectedImages[position] = uri
                    }
                    ifSingleImageSelected = true
                    updateImageViews()
                }
            }
        }
    }

    // Track which image was clicked
    private var clickedImagePosition = -1



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImagesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

        if (viewModel.images.value?.isNotEmpty() == true) {
            selectedImages = viewModel.images.value as MutableList<Uri>
            updateImageViews()
        }

        binding.uploadButton.setOnClickListener {
            checkPermissionsAndOpenGallery()
        }

        binding.nextButton.setOnClickListener {
            if (selectedImages.isNotEmpty()) {
                viewModel.images.value = selectedImages
                findNavController().navigate(R.id.action_images_to_price)
            } else {
                // Show error message if no images selected
                binding.uploadButton.error = "Please select at least one image"
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.img1.setOnClickListener {
            clickedImagePosition = 0
            openSingleImageGallery()
        }

        binding.img2.setOnClickListener {
            clickedImagePosition = 1
            openSingleImageGallery()
        }

        binding.img3.setOnClickListener {
            clickedImagePosition = 2
            openSingleImageGallery()
        }

        binding.img4.setOnClickListener {
            clickedImagePosition = 3
            openSingleImageGallery()
        }

        binding.img5.setOnClickListener {
            clickedImagePosition = 4
            openSingleImageGallery()
        }

        binding.img6.setOnClickListener {
            clickedImagePosition = 5
            openSingleImageGallery()
        }
    }

    private fun checkPermissionsAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGalleryForImages()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // Explain why permission is needed
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else {
            openGalleryForImages()
        }
    }

    // Add this to your fragment
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGalleryForImages()
        } else {
            // Show explanation why permission is needed
        }
    }

    // Opens gallery for the multiple images
    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"

        // For Android 10+ we need to use this approach
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }

        galleryLauncher.launch(intent)
    }

    // Opens gallery for the single image
    private fun openSingleImageGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            // Don't allow multiple selection
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
        }
        singleImageLauncher.launch(intent)
    }

    private fun updateImageViews() {
        if(!ifSingleImageSelected) {    // only clear every image if multiple images are selected
            // Clear all images first
            clearAllImageViews()
        }

        // This will make LinearLayout1 and its needed image views visible
        if (selectedImages.size in 1..3) {
            var size = selectedImages.size
            binding.linearLayout1.visibility = View.VISIBLE
            binding.img1.visibility = View.VISIBLE
            size--
            if (size != 0) {
                binding.img2.visibility = View.VISIBLE
                size--
            }
            if (size != 0) {
                binding.img3.visibility = View.VISIBLE
            }
        }
        // This will make LinearLayout2 and its needed image views visible
        else if (selectedImages.size in 4..6) {     // meaning already upside 3 images should be visible hence we are subtracting size 3 and making them visible by default
            var size = selectedImages.size - 3
            binding.linearLayout1.visibility = View.VISIBLE // previous layout
            binding.linearLayout2.visibility = View.VISIBLE
            binding.img1.visibility = View.VISIBLE  // previous layout image
            binding.img2.visibility = View.VISIBLE  // previous layout image
            binding.img3.visibility = View.VISIBLE  // previous layout image

            binding.img4.visibility = View.VISIBLE
            size--
            if (size != 0) {
                binding.img5.visibility = View.VISIBLE
                size--
            }
            if (size != 0) {
                binding.img6.visibility = View.VISIBLE
            }
        }
        // Set selected images
        for (i in selectedImages.indices) {
            when (i) {
                0 -> loadImage(binding.img1, selectedImages[i])
                1 -> loadImage(binding.img2, selectedImages[i])
                2 -> loadImage(binding.img3, selectedImages[i])
                3 -> loadImage(binding.img4, selectedImages[i])
                4 -> loadImage(binding.img5, selectedImages[i])
                5 -> loadImage(binding.img6, selectedImages[i])
            }
        }
    }

    private fun loadImage(imageView: ImageView, uri: Uri) {
        imageView.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(imageView)
    }

    private fun clearAllImageViews() {
        binding.linearLayout1.visibility = View.GONE
        binding.linearLayout2.visibility = View.GONE
        binding.img1.visibility = View.GONE
        binding.img2.visibility = View.GONE
        binding.img3.visibility = View.GONE
        binding.img4.visibility = View.GONE
        binding.img5.visibility = View.GONE
        binding.img6.visibility = View.GONE
    }

    private fun getClickedImagePosition(): Int {
        return clickedImagePosition
    }

}