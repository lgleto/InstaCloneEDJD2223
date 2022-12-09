package ipca.example.instaclone

import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

class Photo {

    var description : String? = null
    var photo : String? = null

    constructor(description: String?, photo: String?) {
        this.description = description
        this.photo = photo
    }


    fun toHashMap () : HashMap<String,Any?> {
       return hashMapOf(
           "description" to description,
           "photo" to  photo
       )
    }

    companion object {
        fun fromDoc (doc:DocumentSnapshot) :Photo {
            return Photo (
                doc.getString("description"),
                doc.getString("photo")
            )
        }
    }




}