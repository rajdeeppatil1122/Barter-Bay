package com.barterbay.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.barterbay.app.databinding.FragmentPriceBinding


class PriceFragment : Fragment() {
    private lateinit var viewModel: PostProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPriceBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

         if(viewModel.price.value != null){
            binding.price.setText(viewModel.price.value)
        }

        binding.nextButton.setOnClickListener{
            viewModel.price.value = binding.price.text.toString()
            findNavController().navigate(R.id.action_price_to_summary)
        }

        return binding.root
    }

}