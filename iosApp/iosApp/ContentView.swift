import UIKit
import SwiftUI
import ComposeApp


struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return createNavController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        NavigationView {
            ComposeView()
                .navigationBarHidden(true)
                .ignoresSafeArea(.keyboard)
//                 .ignoresSafeArea(.container, edges: .bottom)
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

func createNavController() -> UINavigationController {
    let composeViewController = MainViewControllerKt.MainViewController()

       let navController = UINavigationController(rootViewController: composeViewController)
       navController.setNavigationBarHidden(true, animated: false) // Hide navigation bar
       navController.interactivePopGestureRecognizer?.isEnabled = true // Enable back gesture
       navController.interactivePopGestureRecognizer?.delegate = nil // Allow swipe back

       return navController

}

