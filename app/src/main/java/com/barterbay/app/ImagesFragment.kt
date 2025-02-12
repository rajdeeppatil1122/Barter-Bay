package com.barterbay.app

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.barterbay.app.databinding.FragmentImagesBinding


class ImagesFragment : Fragment() {
    private lateinit var viewModel: PostProductViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentImagesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

        if(viewModel.images.value != null){
            binding.images.setText(viewModel.images.value) // Whenever, the next button is pressed, it will call the nav_graph (for navigation + animation), and in this process, the nav_graph will call the fragment again (because that's how the navigation works), and in the fragment, it will overwrite the text="" value to default, i.e. "Hello blank fragment". To stop this, upside we have written a code to check if the text is not null & re-rewrite the latest modified value of text (and we are sure that it will never be null, so that part of code will be executed even when nothing is edited and also when something is edited so that the latest modified value of text="" will be always written to text itself)
        }

        binding.nextButton.setOnClickListener{
            viewModel.images.value = binding.images.text.toString()
            findNavController().navigate(R.id.action_images_to_price)   // Whenever, the next button is pressed, it will call the nav_graph (for navigation + animation), and in this process, the nav_graph will call the fragment again (because that's how the navigation works), and in the fragment, it will overwrite the text="" value to default, i.e. "Hello blank fragment". To stop this, upside we have written a code to check if the text is not null & re-rewrite the latest modified value of text (and we are sure that it will never be null, so that part of code will be executed even when nothing is edited and also when something is edited so that the latest modified value of text="" will be always written to text itself)
        }

        return binding.root
    }

}