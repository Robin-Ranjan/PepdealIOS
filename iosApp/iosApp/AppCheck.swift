import Firebase
import FirebaseAppCheck

@objc class AppCheckHelper:NSObject{
    @objc static func getAppCheckToken(completion: @escaping (String?) -> Void){
        AppCheck.appCheck().token(forcingRefresh: false){ token ,error in guard let token = token?.token else {
            print("Failed to get App Check token: \(error?.localizedDescription ?? "Unknown error")")
            completion(nil)
            return
        }
            completion(token)
        }
    }
}
