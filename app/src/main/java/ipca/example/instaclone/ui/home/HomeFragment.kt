package ipca.example.instaclone.ui.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import ipca.example.instaclone.Photo
import ipca.example.instaclone.R
import ipca.example.instaclone.databinding.FragmentHomeBinding
import java.io.InputStream

class HomeFragment : Fragment() {

    var photos = arrayListOf<Photo>()
    val db = Firebase.firestore
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddPhoto.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_addPhotoFragment)
        }
        val adapter = PhotoAdapter()
        binding.listViewPhotos.adapter = adapter

        db.collection("photos")
            .addSnapshotListener { value, e ->
                if (e != null) {

                    return@addSnapshotListener
                }

                photos.clear()
                for (doc in value!!) {
                    val item = Photo.fromDoc(doc)
                    photos.add(item)
                }

                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PhotoAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return photos.size
        }

        override fun getItem(p0: Int): Any {
            return photos[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        @SuppressLint("SetTextI18n")
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val rootView = layoutInflater.inflate(R.layout.row_photo, p2, false)

            val textViewRowDescription = rootView.findViewById<TextView>(R.id.textViewRowDescription)
            val imageViewRowPhoto = rootView.findViewById<ImageView>(R.id.imageViewRowPhoto)


            val storage = Firebase.storage
            val storageRef = storage.reference

            var islandRef = storageRef.child("images/${photos[p0].photo}")

            val ONE_MEGABYTE: Long = 10024 * 1024
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                val inputStream = it.inputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageViewRowPhoto.setImageBitmap(bitmap)
            }.addOnFailureListener {
                // Handle any errors
            }


            textViewRowDescription.text = photos[p0].description

            return rootView
        }

    }


}