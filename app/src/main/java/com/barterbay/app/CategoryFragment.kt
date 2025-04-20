package com.barterbay.app

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barterbay.app.databinding.FragmentCategoryBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CategoryFragment : Fragment(), CategoryAdapter.OnCategorySelectedListener {
    private lateinit var viewModel: PostProductViewModel

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private var binding: FragmentCategoryBinding? = null

    private val categoryList = listOf(
        CategoryModel("Action Cameras", "actioncameras.json"),
        CategoryModel("Apple MacBook Variants", "applemacbookvarients.json"),
        CategoryModel("Art Supplies", "artsupplies.json"),
        CategoryModel("Backpacks", "backpacks.json"),
        CategoryModel("Bedsheets", "bedsheets.json"),
        CategoryModel("Bicycles", "bicycles.json"),
        CategoryModel("Board Games", "boardgames.json"),
        CategoryModel("Books", "books.json"),
        CategoryModel("Bookshelves", "bookshelves.json"),
        CategoryModel("Calculators", "calculators.json"),
        CategoryModel("Cameras", "cameras.json"),
        CategoryModel("Caps", "caps.json"),
        CategoryModel("Chargers", "chargers.json"),
        CategoryModel("Clipboards", "clipboards.json"),
        CategoryModel("Clocks", "clocks.json"),
        CategoryModel("Coats", "coats.json"),
        CategoryModel("Coffee Makers", "coffeemakers(forlate-nightstudying&hustlemode).json"),
        CategoryModel("Crockery", "crockery.json"),
        CategoryModel("Curtains", "curtains.json"),
        CategoryModel("Dresses", "dresses.json"),
        CategoryModel("Dumbbells", "dumbbells.json"),
        CategoryModel("Engineering Notes", "engineeringnotes.json"),
        CategoryModel("Fans", "fans.json"),
        CategoryModel("Fashion Shoes", "fashionshoes.json"),
        CategoryModel("Fashion Sunglasses", "fashionsunglasses.json"),
        CategoryModel("Fashion Watches", "fashionwatches.json"),
        CategoryModel("Fiction Novels", "fictionnovels.json"),
        CategoryModel("Fitness Bands", "fitnessbands.json"),
        CategoryModel("Flash Drives", "flashdrives.json"),
        CategoryModel("Flashlights", "flashlights.json"),
        CategoryModel("Furniture (Desks & Chairs)", "furnituredesksandchairs.json"),
        CategoryModel("Gaming Consoles", "gamingconsoles.json"),
        CategoryModel("Gloves", "gloves.json"),
        CategoryModel("Graphing Paper", "graphingpaper.json"),
        CategoryModel("Hair Dryers", "hairdryers.json"),
        CategoryModel("Hair Straighteners", "hairstraighteners.json"),
        CategoryModel("Hard Drives", "harddrives.json"),
        CategoryModel("HDMI Cables", "hdmi.json"),
        CategoryModel("Headphones", "headphones.json"),
        CategoryModel("Helmets", "helmets.json"),
        CategoryModel("Highlighters", "highlighters.json"),
        CategoryModel("Home Appliances", "homeappliances.json"),
        CategoryModel("Hoodies", "hoodies.json"),
        CategoryModel("Jeans", "jeans.json"),
        CategoryModel("Jewelry", "jewelry.json"),
        CategoryModel("Journals", "journals.json"),
        CategoryModel("Keyboards", "keyboards.json"),
        CategoryModel("Kitchen Appliances", "kitchenappliances.json"),
        CategoryModel("Laptops", "laptops.json"),
        CategoryModel("Literature Books", "literaturebooks.json"),
        CategoryModel("Makeup Kits", "makeupkits.json"),
        CategoryModel("Markers", "markers.json"),
        CategoryModel("Mattresses", "mattresses.json"),
        CategoryModel("Monitors", "monitors.json"),
        CategoryModel("Mousepads", "mousepads.json"),
        CategoryModel("Musical Instruments", "musicalinstruments.json"),
        CategoryModel("Pendrives", "pendrive.json"),
        CategoryModel("Pillows", "pillows.json"),
        CategoryModel("Planners", "planners(forstudy,assignments,projects).json"),
        CategoryModel("Posters", "posters(fordormhostelsrooms,studyspaces).json"),
        CategoryModel("Power Banks", "powerbanks.json"),
        CategoryModel("Printer Accessories", "printeraccessories.json"),
        CategoryModel("Projectors", "projectors.json"),
        CategoryModel("Room Decor", "roomdecor.json"),
        CategoryModel("Rugs", "rugs.json"),
        CategoryModel("Safety Goggles", "safetygoggles.json"),


//        These 5 causes problems
//
//        CategoryModel("Software Licenses", "softwarelicense.json"),
//        CategoryModel("Sports Equipment", "sportsequipment.json"),
//        CategoryModel("Sports Jerseys", "sportsjerseys.json"),
//        CategoryModel("Smart Speakers", "smartspeakers.json"),


        CategoryModel("Scanners", "scanners.json"),
        CategoryModel("Screwdrivers", "screwdrivers.json"),
        CategoryModel("Sewing Machines", "sewingmachines.json"),
        CategoryModel("Skateboards", "skateboards.json"),
        CategoryModel("Smartphones", "smartphones.json"),


        CategoryModel("T-Shirts", "tshirts.json"),
        CategoryModel("Tupperware", "tupperware.json"),
        CategoryModel("Umbrellas", "umbrellas.json"),
        CategoryModel("USB Devices", "usb.json"),
        CategoryModel("Vehicles", "vehicles.json"),
        CategoryModel("VR Headsets", "vrheadsets.json"),
        CategoryModel("Wallets", "wallets.json"),
        CategoryModel("Watches", "fashionwatches.json"),
        CategoryModel("Water Bottles", "waterbottles.json"),
        CategoryModel("Water Filters", "waterfilters.json"),
        CategoryModel("White Boards", "whiteboards.json"),
        CategoryModel("Yoga Mats", "yogamats.json"),
        CategoryModel("Other", "other.json")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container,false)   // Inflate the layout for this fragment while binding
        // viewModel initialization
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)


        // Setup RecyclerView
        categoryAdapter = CategoryAdapter(categoryList, this)
        binding!!.categoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        val spacingItemDecoration =
            GridSpacingItemDecoration(13, 0) // Adjust vertical and horizontal spacing as needed
        binding!!.categoryRecyclerView.addItemDecoration(spacingItemDecoration)
        binding!!.categoryRecyclerView.adapter = categoryAdapter


        // Restore selection after adapter is set up
        viewModel.selectedCategory.value?.let { category ->
            val position = categoryList.indexOfFirst { it.name == category.name }
            if (position != -1) {
                // Set the selected position in adapter
                categoryAdapter.setSelectedPosition(position)

                // Trigger the UI updates
                viewModel.categoryLottieName.value?.let {
                    onCategorySelected(true, category.name,
                        it
                    )
                }

                // Scroll to the selected position
                binding!!.categoryRecyclerView.post {
                    binding!!.categoryRecyclerView.scrollToPosition(position)
                }
            }
        }




        // Search functionality
        binding!!.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                categoryAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding!!.selectCategoryNextBtn.setOnClickListener {
            context?.toast("Clicked")
            findNavController().navigate(com.barterbay.app.R.id.action_category_to_images)
        }


        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCategorySelected(isSelected: Boolean, categoryName: String, categoryLottie: String) {
        binding?.selectCategoryNextBtn?.apply {     // We are talking about 'CardView' here (the black button with fading-out text)...
            if (isSelected) {
                // Save the selected category to ViewModel
                val category = categoryList.firstOrNull { it.name == categoryName }
                viewModel.selectedCategory.value = category

                // Save the selected category's lottie view in ViewModel
                viewModel.categoryLottieName.value = categoryLottie

                if (visibility != View.VISIBLE) {
                    // First time selection → Fade in the button
                    alpha = 0f
                    visibility = View.VISIBLE
                    isEnabled = true  // Ensure it's enabled when visible
                    animate().alpha(1f).setDuration(200).start()
                }

                // **Only fade text if it's changing**
                if (binding!!.selectCategoryText.text != categoryName) {
                    binding!!.selectCategoryText.apply {
                        animate().alpha(0f).setDuration(100).withEndAction {
                            text = categoryName + "  -  Tap Here" // Update text
                            animate().alpha(1f).setDuration(200).start() // Fade in new text
                        }.start()
                    }
                }

                // Ensure the selected item is highlighted
                val selectedPos = categoryAdapter.getSelectedPosition()
                if (selectedPos != RecyclerView.NO_POSITION) {
                    categoryAdapter.notifyItemChanged(selectedPos)
                }

            } else {
                // When deselected (clicked the same category again), fade out the button
                animate().alpha(0f).setDuration(200).withEndAction {
                    visibility = View.INVISIBLE
                    isEnabled = false
                }.start()
            }
        }
    }


    // Create an extension function for toast:
    fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}