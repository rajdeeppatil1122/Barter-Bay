package com.barterbay.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.barterbay.app.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {
    private lateinit var viewModel: PostProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSummaryBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

        binding.categoryTxt.text = viewModel.category.value
        binding.priceTxt.text = viewModel.price.value
        binding.imagesTxt.text = viewModel.images.value

        return binding.root
    }

}