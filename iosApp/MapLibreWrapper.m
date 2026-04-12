//
//  MapLibreWrapper.m
//  iosApp
//
//  Created by Saionji on 08.04.2026.
//

#import "MapLibreWrapper.h"
#import <MapLibre/MapLibre.h>
#import <CoreLocation/CoreLocation.h>

@interface MapLibreWrapper ()
@property (nonatomic, strong) MLNMapView *mapView;
@property (nonatomic, strong) NSMutableArray<MLNPointAnnotation *> *currentMarkers;
@end

@implementation MapLibreWrapper

- (UIView *)createMapView {
    self.mapView = [[MLNMapView alloc] init];
    self.mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    self.mapView.styleURL = [NSURL URLWithString:@"https://tiles.openfreemap.org/styles/liberty"];
    
    CLLocationCoordinate2D center = CLLocationCoordinate2DMake(55.7558, 37.6173);
    
    return self.mapView;
}

- (void)moveTo:(double)lat lon:(double)lon zoom:(double)zoom {
    CLLocationCoordinate2D center = CLLocationCoordinate2DMake(lat, lon);
    [self.mapView setCenterCoordinate:center zoomLevel:zoom animated:YES];
}

- (void)zoomIn {
    [self.mapView setZoomLevel:self.mapView.zoomLevel + 1.0 animated:YES];
}

- (void)zoomOut {
    [self.mapView setZoomLevel:self.mapView.zoomLevel - 1.0 animated:YES];
}

- (double)getZoom {
    return self.mapView.zoomLevel;
}

- (double)getCenterLat {
    return self.mapView.centerCoordinate.latitude;
}

- (double)getCenterLon {
    return self.mapView.centerCoordinate.longitude;
}

// === Маркеры ===

- (void)setMarkers:(NSArray<NSDictionary *> *)markers {
    // Удаляем старые маркеры
    [self clearMarkers];
    
    // Добавляем новые
    for (NSDictionary *marker in markers) {
        NSString *markerId = marker[@"id"];
        double lat = [marker[@"lat"] doubleValue];
        double lon = [marker[@"lon"] doubleValue];
        
        MLNPointAnnotation *annotation = [[MLNPointAnnotation alloc] init];
        annotation.coordinate = CLLocationCoordinate2DMake(lat, lon);
        annotation.title = markerId;
        
        [self.mapView addAnnotation:annotation];
        [self.currentMarkers addObject:annotation];
    }
}

- (void)clearMarkers {
    [self.mapView removeAnnotations:self.currentMarkers];
    [self.currentMarkers removeAllObjects];
}

// === Delegate для стиля маркеров ===

- (MLNAnnotationView *)mapView:(MLNMapView *)mapView viewForAnnotation:(id<MLNAnnotation>)annotation {
    if (![annotation isKindOfClass:[MLNPointAnnotation class]]) {
        return nil;
    }
    
    // Сщздаем круглый маркер
    MLNAnnotationView *annotationView = [[MLNAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"marker"];
    annotationView.frame = CGRectMake(0, 0, 22, 22);
    annotationView.layer.cornerRadius = 11;
    annotationView.layer.borderWidth = 1;
    annotationView.layer.borderColor = [UIColor darkGrayColor].CGColor;
    annotationView.backgroundColor = [UIColor colorWithRed:0 green:0.5 blue:1 alpha:0.9]; // TODO: цвет из маркера
    
    return  annotationView;
}
@end
