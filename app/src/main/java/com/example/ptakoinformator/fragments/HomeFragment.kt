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
import android.database.Cursor
import android.graphics.Matrix
import java.net.URI
import android.os.ParcelFileDescriptor
import android.util.Size
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.data.Classification
import java.io.FileDescriptor


class HomeFragment : Fragment() {
    lateinit var currentPhotoPath: String
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
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

        viewModel.lastBird.observe(viewLifecycleOwner){

            val bitmap = BitmapFactory.decodeFile(it?.photoUri)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap,200,200)
            bindClassifiedBirdView(thumbnail, it?.classification, it?.date)

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

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            //val selectedImage = it.data?.getParcelableExtra<Bitmap>("data")
            val selectedImage = it.data?.data
            Log.d("DBG",selectedImage.toString())
            val result=viewModel.classifyBird(selectedImage!!,requireContext())
            currentPhotoPath=getRealPathFromURI(selectedImage)!!
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap,200,200)
            Log.d("DBG",currentPhotoPath)
            val bird = Bird(0,
                currentPhotoPath,
                getCurrentDate(),
                "",
                Classification(
                    result[0].score,result[0].label,
                    result[1].score,result[1].label,
                    result[2].score,result[2].label)
            )
            viewModel.createBird(bird)
            bindClassifiedBirdView(thumbnail!!,bird.classification, getCurrentDate())
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
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap,200,200)
            val rotateThumbnail= rotateBitmap(thumbnail)
            val uri=Uri.fromFile(File(currentPhotoPath))
            Log.d("ADK",uri.toString())
            val result=viewModel.classifyBird(uri,requireContext())
            val bird = Bird(0,
                currentPhotoPath,
                getCurrentDate(),
                "",
                Classification(
                    result[0].score,result[0].label,
                    result[1].score,result[1].label,
                    result[2].score,result[2].label)
            )
            viewModel.createBird(bird)
            bindClassifiedBirdView(rotateThumbnail!!,bird.classification, getCurrentDate())
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

    private fun rotateBitmap(source: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(90F)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun bindClassifiedBirdView(bitmap: Bitmap?, result: Classification?, date: String?){
        binding.classifiedBirdView.setPhoto(bitmap)
        binding.classifiedBirdView.setFirstResult(result?.mainClassification, (result?.mainProbability?.times(
            100
        )))
        binding.classifiedBirdView.setSecondResult(result?.secondClassification, (result?.secondProbability?.times(
            100
        )))
        binding.classifiedBirdView.setThirdResult(result?.thirdClassification, (result?.thirdProbability?.times(
            100
        )))
        binding.classifiedBirdView.setDate(date)
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