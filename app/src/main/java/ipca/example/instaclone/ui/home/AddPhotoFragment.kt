package ipca.example.instaclone.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import ipca.example.instaclone.databinding.FragmentAddPhotoBinding
import ipca.example.instaclone.Photo
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddPhotoFragment : Fragment() {

    private var _binding: FragmentAddPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dispatchTakePictureIntent()

        val db = Firebase.firestore
        binding.buttonPublish.setOnClickListener {

            uploadFile { filename ->
                filename?.let {


                    val photo = Photo( binding.editTextPhotoDescription.text.toString(), it)

                    db.collection("photos")
                        .add(photo.toHashMap())
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                }

            }


        }
    }

    fun uploadFile(callback:(String?)->Unit){
        val storage = Firebase.storage
        val storageRef = storage.reference
        val file = Uri.fromFile(File(currentPhotoPath))

        var metadata = storageMetadata {
            contentType = "image/jpeg"
        }

        val uploadTask = storageRef.child("images/${file.lastPathSegment}").putFile(file)
        uploadTask.addOnProgressListener { task ->
            val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
            Log.d(TAG, "Upload is $progress% done")
        }
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback.invoke(file.lastPathSegment)
            }else{
                callback.invoke(null)
            }

        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dispatchTakePictureIntent(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{ takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go

                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "ipca.example.instaclone.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }

            }
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //val imageBitmap = data?.extras?.get("data") as Bitmap

            BitmapFactory.decodeFile(currentPhotoPath).apply {
                binding.imageViewPhoto.setImageBitmap(this)
            }
            //binding.imageViewPhoto.setImageBitmap(imageBitmap)
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1001
        const val TAG = "AddPhotoFragment"
    }




}