package com.example.ptakoinformator.fragments


import android.Manifest
import android.app.Activity
import android.content.Intent

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ptakoinformator.TFLiteModelManager
import com.example.ptakoinformator.databinding.HomeFragmentBinding

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

import org.tensorflow.lite.support.image.TensorImage


import java.util.*


class HomeFragment : Fragment() {
    lateinit var currentPhotoPath: String
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonShowReports.setOnClickListener{
            it.findNavController().navigate(HomeFragmentDirections.actionFragmentHomeToFragmentReports())
        }
        binding.buttonUploadPhoto.setOnClickListener { pickImageGallery() }

        binding.buttonTakePhoto.setOnClickListener { takePhoto() }
    }


    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getImage.launch(intent)
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            //val selectedImage = it.data?.getParcelableExtra<Bitmap>("data")
            val selectedImage = it.data?.data
            //binding.imageViewSelectedImage.setImageURI(selectedImage)
            Log.d("DBG",selectedImage.toString())
            if(Build.VERSION.SDK_INT >= 28) {
                val source: ImageDecoder.Source = ImageDecoder.createSource(
                    requireActivity().contentResolver,
                    selectedImage!!
                )
                var bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val model = TFLiteModelManager.getInstance(requireContext())
                val outputs = model.process(TensorImage.fromBitmap(bitmap))
                val probabilities = outputs.probabilityAsCategoryList
                Log.d("DBG",probabilities.toString())
            }
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir= requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun takePhoto() {
        if(allPermissionsGranted()){
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        Toast.makeText(requireContext(),
                            "Nie udało się zapisać zdjęcia",
                            Toast.LENGTH_SHORT).show()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.ptakoinformator",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        getPhoto.launch(takePictureIntent)
                    }
                }
            }
        }
        else{
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val getPhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            binding.imageViewSelectedImage.setImageBitmap(
                ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(currentPhotoPath),64,64))
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (permission == true ) {
                takePhoto()
            } else {
                Toast.makeText(requireContext(),
                    "Nie przyznano dostępu",
                    Toast.LENGTH_SHORT).show()
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS=101
    }

}