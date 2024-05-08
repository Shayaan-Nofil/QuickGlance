import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Snap: Parcelable  {
    var id: String = ""
    var senderid: String = ""
    var content: String = ""
    var tag: String = ""
    var time: String = ""
}