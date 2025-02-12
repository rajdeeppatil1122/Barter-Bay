package com.barterbay.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barterbay.app.databinding.FragmentCategoryBinding


class CategoryFragment : Fragment() {
    private lateinit var viewModel: PostProductViewModel

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText

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
//        CategoryModel("Watches", "watches.json"),
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

        CategoryModel("Water Bottles", "waterbottles.json"),
        CategoryModel("Water Filters", "waterfilters.json"),
        CategoryModel("White Boards", "whiteboards.json"),
        CategoryModel("Yoga Mats", "yogamats.json")
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCategoryBinding.inflate(inflater, container, false)   // Inflate the layout for this fragment while binding

        // viewModel initialization
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

//        if(viewModel.category.value != null){
//            binding.categoryInput.setText(viewModel.category.value)
//        }
//
//        binding.nextButton.setOnClickListener {
//            viewModel.category.value = binding.categoryInput.text.toString()
//            findNavController().navigate(R.id.action_category_to_images)
//        }


        // Setup RecyclerView
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.categoryRecyclerView.adapter = categoryAdapter

        // Search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                categoryAdapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

}