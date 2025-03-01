package com.pepdeal.infotech.location

import platform.CoreLocation.CLLocationManagerDelegateProtocol
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject

//// Create an iOS implementation of LocationTracker using CoreLocation.
//class IOSLocationTracker : NSObject(), LocationTracker, CLLocationManagerDelegateProtocol {
//
//    private val locationManager = CLLocationManager().apply {
//        desiredAccuracy = kCLLocationAccuracyBest
//        // Set delegate to self (this object)
//        delegate = this@IOSLocationTracker
//    }
//
//    override fun getLocationFlow(): Flow<Location?> = callbackFlow {
//        locationManager.requestWhenInUseAuthorization()
//        locationManager.startUpdatingLocation()
//
//        awaitClose { locationManager.stopUpdatingLocation() }
//    }
//
//    // Delegate method called by CoreLocation when new locations are available.
//    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
//        val clLocation = didUpdateLocations.firstOrNull() as? CLLocation
//        if (clLocation != null) {
//            trySend(Location(clLocation.coordinate.latitude, clLocation.coordinate.longitude))
//        }
//        locationManager.stopUpdatingLocation()
//    }
//}
