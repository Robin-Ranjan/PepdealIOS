
import kotlinx.serialization.Serializable


@Serializable
data class UserMaster(
    val userId:String = "",
    val userName:String ="",
    val mobileNo:String ="",
    val emailId:String ="",
    val password:String ="",
    val fcmToken: String = "",
    val deviceToken:String ="", // from which device has been login
    var isActive:String ="",   // 0-> user is active, 1-> user is blocked
    val userStatus:String ="", // 1-> if shop is open by user , 0-> if not opened
    val createdAt:String ="",  // date and time -> yyyy-mm-dd , time 24 hour format
    val updatedAt:String =""
)