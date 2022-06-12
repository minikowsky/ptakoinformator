package com.example.ptakoinformator.fragments


import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.ptakoinformator.TFLiteModelManager
import com.example.ptakoinformator.databinding.HomeFragmentBinding
import com.example.ptakoinformator.viewmodels.HomeViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import android.R.attr.path
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Matrix
import android.media.ExifInterface
import android.graphics.drawable.GradientDrawable
import java.net.URI
import android.os.ParcelFileDescriptor
import android.util.Size
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.viewbinding.ViewBinding
import com.example.ptakoinformator.R
import com.example.ptakoinformator.customview.ClassifiedBirdView
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.data.Classification
import com.example.ptakoinformator.databinding.HomeFragmentHorizontalBinding
import java.io.FileDescriptor


class HomeFragment : Fragment() {
    lateinit var currentPhotoPath: String
    private var _binding: ViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            _binding = HomeFragmentHorizontalBinding.inflate(inflater, container, false)
            return binding.root
        }
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_show_reports).setOnClickListener{
            it.findNavController().navigate(HomeFragmentDirections.actionFragmentHomeToFragmentReports())
        }
        view.findViewById<Button>(R.id.button_upload_photo).setOnClickListener{pickImageGallery()}
        view.findViewById<AppCompatImageButton>(R.id.button_settings).setOnClickListener {
            it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment())
        }
        view.findViewById<ImageButton>(R.id.button_take_photo).setOnClickListener { takePhoto() }

        var path: String? = null

        viewModel.lastBird.observe(viewLifecycleOwner){
            if(it==null){
                view.findViewById<ClassifiedBirdView>(R.id.classified_bird_view).visibility=View.GONE
            }
            else{
                view.findViewById<ClassifiedBirdView>(R.id.classified_bird_view).visibility=View.VISIBLE
                bindClassifiedBirdView(it.photoUri, it.classification, it.date)
                path = it.photoUri
            }
        }

        view.findViewById<View>(R.id.classified_bird_view).setOnClickListener {
            val exif = ExifInterface(path!!)
            var geolocation = FloatArray(2)
            exif.getLatLong(geolocation)
            Toast.makeText(requireContext(), geolocation.contentToString(), Toast.LENGTH_SHORT).show()
        }
    }


    private fun storagePermissionsGranted() = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).all{
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickImageGallery() {
        if(storagePermissionsGranted()){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getImage.launch(intent)
        }
        else{
            requestStoragePermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
        if (it.resultCode == Activity.RESULT_OK) {
            val selectedImage = it.data?.data
            viewModel.classifyBird(selectedImage!!,requireContext())
            currentPhotoPath=getRealPathFromURI(selectedImage!!)!!
            var bird : Bird
            viewModel.categoryList.observe(viewLifecycleOwner) {result->
                bird = Bird(
                    0,
                    currentPhotoPath,
                    getCurrentDate(),
                    "",
                    Classification(
                        result[0].score, result[0].label,
                        result[1].score, result[1].label,
                        result[2].score, result[2].label
                    )
                )
                viewModel.createBird(bird)
                bindClassifiedBirdView(currentPhotoPath, bird.classification, getCurrentDate())
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
            val uri=Uri.fromFile(File(currentPhotoPath))
            Log.d("ADK",uri.toString())
            viewModel.classifyBird(uri,requireContext())
            var bird : Bird
            viewModel.categoryList.observe(viewLifecycleOwner) {result->
                bird = Bird(
                    0,
                    currentPhotoPath,
                    getCurrentDate(),
                    "",
                    Classification(
                        result[0].score, result[0].label,
                        result[1].score, result[1].label,
                        result[2].score, result[2].label
                    )
                )
                viewModel.createBird(bird)
                bindClassifiedBirdView(currentPhotoPath, bird.classification, getCurrentDate())
            }
        }
    }


    private fun getRealPathFromURI(contentURI: Uri): String? {
        val filePath: String?
        val cursor: Cursor? = requireActivity().contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            filePath = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = cursor.getString(idx)
            cursor.close()
        }
        return filePath
    }

    private val requestStoragePermission=
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                if (!it.value) {
                    Toast.makeText(requireContext(),
                        "Nie przyznano dostępu",
                        Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
            }
            pickImageGallery()
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


    private fun bindClassifiedBirdView(path: String?, result: Classification?, date: String?){
        view?.findViewById<ClassifiedBirdView>(R.id.classified_bird_view)?.setPhoto(path)
        view?.findViewById<ClassifiedBirdView>(R.id.classified_bird_view)?.setFirstResult(result?.mainClassification, (result?.mainProbability?.times(
            100
        )))
        view?.findViewById<ClassifiedBirdView>(R.id.classified_bird_view)?.setSecondResult(result?.secondClassification, (result?.secondProbability?.times(
            100
        )))
        view?.findViewById<ClassifiedBirdView>(R.id.classified_bird_view)?.setThirdResult(result?.thirdClassification, (result?.thirdProbability?.times(
            100
        )))
        view?.findViewById<ClassifiedBirdView>(R.id.classified_bird_view)?.setDate(date)
    }


    private fun getCurrentDate(): String{
        val simpleDateFormat= SimpleDateFormat("dd-MM-yyyy HH:MM:SS")
        return simpleDateFormat.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS=101
    }

}