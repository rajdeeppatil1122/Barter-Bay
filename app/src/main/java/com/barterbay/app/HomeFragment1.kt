package com.barterbay.app

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.barterbay.app.databinding.FragmentHome1Binding

class HomeFragment1 : Fragment() {
    private var binding : FragmentHome1Binding? = null  // here, the FragmentHome1Binding itself contains all the views present in the fragment.FragmentHome1Binding is currently a class now.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflating the layout for this fragment
        binding = FragmentHome1Binding.inflate(inflater, container, false)  // inflater for inflating THE XML VIEWS (as a single entity called as class) into the 'binding' variable.
        return binding!!.root       // kishmish, it means that binding should not be null, it must have view hierarchy which is present in the 'class' file, which was essentially called as: XML views
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.floatingActionBtn.setOnClickListener{     // '?' - if binding is remains null, save my app from crashing
                                                            // '!!' - I am damn sure that it cannot be null. If you find it null, crash the app. I challenge you.
                                                            // Here, we are using !! because we are sure that binding variable cannot be null. That's because, in onCreateView() method we have inflated (assigned) binding variable with the required values of XML views which were present in the FragmentHome1Binding class. Hence, binding variable must has all XML views kundli (information). Understood kishmish?
            var intent = Intent(view.context, TestActivity::class.java)
            startActivity(intent)
        }
    }

}