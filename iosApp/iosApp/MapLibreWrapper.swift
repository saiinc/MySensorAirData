//
//  MapLibreWrapper.swift
//  iosApp
//
//  Created by Saionji on 08.04.2026.
//

import Foundation
import UIKit
import MapLibre
import CoreLocation

@objc public class MapLibreWrapper: NSObject {

    private var mapView: MLNMapView?

    @objc public func createMapView() -> UIView {
        let map = MLNMapView(frame: .zero)
        map.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        map.styleURL = URL(string: "https://demotiles.maplibre.org/style.json")

        // Начальная позиция (Москва)
        let center = CLLocationCoordinate2D(latitude: 55.7558, longitude: 37.6173)
        map.setCenter(center, zoomLevel: 10.0, animated: false)

        mapView = map
        return map
    }

    @objc public func moveTo(lat: Double, lon: Double, zoom: Double) {
        let center = CLLocationCoordinate2D(latitude: lat, longitude: lon)
        mapView?.setCenter(center, zoomLevel: zoom, animated: true)
    }

    @objc public func zoomIn() {
        guard let map = mapView else { return }
        map.setZoomLevel(map.zoomLevel + 1.0, animated: true)
    }

    @objc public func zoomOut() {
        guard let map = mapView else { return }
        map.setZoomLevel(map.zoomLevel - 1.0, animated: true)
    }

    @objc public func getZoom() -> Double {
        return mapView?.zoomLevel ?? 10.0
    }

    @objc public func getCenterLat() -> Double {
        return mapView?.centerCoordinate.latitude ?? 0.0
    }

    @objc public func getCenterLon() -> Double {
        return mapView?.centerCoordinate.longitude ?? 0.0
    }
}
