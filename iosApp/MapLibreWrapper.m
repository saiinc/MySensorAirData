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

- (instancetype)init {
    self = [super init];
    if (self) {
        _currentMarkers = [NSMutableArray array];
    }
    return self;
}

- (UIView *)createMapView {
    self.mapView = [[MLNMapView alloc] init];
    self.mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    self.mapView.styleURL = [NSURL URLWithString:@"https://tiles.openfreemap.org/styles/liberty"];
    self.mapView.delegate = self;

    CLLocationCoordinate2D center = CLLocationCoordinate2DMake(55.7558, 37.6173);
    [self.mapView setCenterCoordinate:center zoomLevel:10.0 animated:NO];
    
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
    NSLog(@"setMarkers called with %lu markers", (unsigned long)[markers count]);
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

- (void)notifyViewportChanged {
    if (!self.onViewportChanged || self.mapView == nil) {
        return;
    }

    MLNCoordinateBounds bounds = [self.mapView visibleCoordinateBounds];
    self.onViewportChanged(
        bounds.ne.latitude,
        bounds.sw.latitude,
        bounds.ne.longitude,
        bounds.sw.longitude,
        self.mapView.zoomLevel
    );
}

// === Delegate для стиля маркеров ===

- (void)mapView:(MLNMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
    [self notifyViewportChanged];
}

- (void)mapViewDidFinishLoadingMap:(MLNMapView *)mapView {
    [self notifyViewportChanged];
}

- (MLNAnnotationView *)mapView:(MLNMapView *)mapView viewForAnnotation:(id<MLNAnnotation>)annotation {
    if (![annotation isKindOfClass:[MLNPointAnnotation class]]) {
        return nil;
    }
    
    // Создаем круглый маркер
    MLNAnnotationView *annotationView = [[MLNAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"marker"];
    annotationView.frame = CGRectMake(0, 0, 22, 22);
    annotationView.layer.cornerRadius = 11;
    annotationView.layer.borderWidth = 1;
    annotationView.layer.borderColor = [UIColor darkGrayColor].CGColor;
    annotationView.backgroundColor = [UIColor colorWithRed:0 green:0.5 blue:1 alpha:0.9]; // TODO: цвет из маркера
    
    return  annotationView;
}
@end
