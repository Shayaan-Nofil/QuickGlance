import java.io.Serializable

class User: Serializable {
    var id: String = ""
    var username: String = ""
    var name:String = "John Doe"
    var email: String = ""
    var number: String = ""
    var birthday: String = ""
    var token: String = ""
    var guest: Boolean = false
    var profilepic: String = "https://firebasestorage.googleapis.com/v0/b/quickglance-7a0ce.appspot.com/o/default_profile.png?alt=media&token=fd19ac57-4d23-46ca-8af7-a7dbc7665155"
}