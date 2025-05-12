
import MapLibre
import MapLibreNavigationCore

//TODO (fabi755): this should part of navigation core project for iOS/Apple platform
class NavigationLocationManager: NSObject, MLNLocationManager, ProgressChangeListener {
    
    var delegate: MLNLocationManagerDelegate?
    
    var headingOrientation: CLDeviceOrientation = .unknown
    
    var authorizationStatus: CLAuthorizationStatus = .notDetermined
    
    func onProgressChange(location: Location, routeProgress: RouteProgress) {
        delegate?.locationManager(self, didUpdate: [location.toAppleLocation()])
    }
    
    func requestAlwaysAuthorization() {
        // No operation.
    }
    
    func requestWhenInUseAuthorization() {
        // No operation.
    }
    
    func startUpdatingLocation() {
        // No operation.
    }
    
    func stopUpdatingLocation() {
        // No operation.
    }
    
    func startUpdatingHeading() {
        // No operation.
    }
    
    func stopUpdatingHeading() {
        // No operation.
    }
    
    func dismissHeadingCalibrationDisplay() {
        // No operation.
    }
}
