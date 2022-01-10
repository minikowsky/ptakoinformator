package com.example.ptakoinformator.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ptakoinformator.R
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}