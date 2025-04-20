package com.barterbay.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.barterbay.app.databinding.FragmentPriceBinding
import java.io.File


class PriceFragment : Fragment() {
    private lateinit var viewModel: PostProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPriceBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

        // Setting the Product Name
        if(viewModel.name.value != null){
            binding.productNameText.text = Editable.Factory.getInstance().newEditable("Product Name - " + viewModel.name.value)
        }

        // Setting the LottieAnimation
        if(viewModel.categoryLottieName.value != null){
            val directory = File(requireContext().filesDir, "ProductCategory")  // finding directory
            val localFile = File(directory, viewModel.categoryLottieName.value.toString())  // finding file in the directory

            if (localFile.exists()) {   // so the file will be get found effortlessly and we will set the lottie view
                try {
                    // Load animation from local storage
                    val jsonString = localFile.readText()
                    binding.priceLottie.setAnimationFromJson(jsonString)    // we set by giving whole jsonfile to work upon
                    binding.priceLottie.playAnimation()
//            context.toast("We are in IF")
                }catch (e: Exception){
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                }

            }
        }

        // Setting product price already set
        if(viewModel.price.value != null){

        }


        // Checks for invalid prices
        binding.priceEditText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    try {
                        val price = s.toString().toDouble()
                        if (price > 100000) {
                            binding.priceInputLayout.error = "Maximum price for barter is 100,000"
                        }
                        else if(price < 1){
                            binding.priceInputLayout.error = "Price for barter cannot be 0"
                        }
                        else {
                            binding.priceInputLayout.error = null
                        }
                    } catch (e: NumberFormatException) {
                        binding.priceInputLayout.error = "Invalid number"
                    }
                }
            }
        })

        binding.continueButton.setOnClickListener{
            findNavController().navigate(R.id.action_price_to_summary)
        }

        return binding.root
    }

}