package com.example.ptakoinformator.fragments


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ptakoinformator.databinding.HomeFragmentBinding


class HomeFragment : Fragment() {


    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonShowReports.setOnClickListener{
            it.findNavController().navigate(HomeFragmentDirections.actionFragmentHomeToFragmentReports())
        }
        binding.buttonUploadPhoto.setOnClickListener { pickImageGallery() }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getImage.launch(intent)
    }
    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            val selectedImage = it.data?.data
            binding.imageViewSelectedImage.setImageURI(selectedImage)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}