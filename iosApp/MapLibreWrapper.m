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
@end

@implementation MapLibreWrapper

- (instancetype)init {
    self = [super init];
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

// === Методы карты ===

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

// === Маркеры с кластеризацией ===

- (void)setMarkers:(NSArray<NSDictionary *> *)markers {
    // Удаляем старые источники и слои
    [self clearMarkerLayers];

    if ([markers count] == 0) return;

    // Создаём features
    NSMutableArray<MLNPointFeature *> *features = [NSMutableArray array];
    for (NSDictionary *marker in markers) {
        MLNPointFeature *feature = [[MLNPointFeature alloc] init];
        double lat = [marker[@"lat"] doubleValue];
        double lon = [marker[@"lon"] doubleValue];
        feature.coordinate = CLLocationCoordinate2DMake(lat, lon);

        int colorInt = [marker[@"colorInt"] intValue];
        feature.attributes = @{
                @"id": marker[@"id"],
                @"colorInt": marker[@"colorInt"],
                @"r": @((colorInt >> 16) & 0xFF),
                @"g": @((colorInt >> 8) & 0xFF),
                @"b": @(colorInt & 0xFF)
        };
        [features addObject:feature];
    }

    // Создаём shape source с кластеризацией
    NSDictionary *options = @{
            MLNShapeSourceOptionClustered: @YES,
            MLNShapeSourceOptionClusterRadius: @40,
            MLNShapeSourceOptionClusterMaxZoomLevel: @12,
            MLNShapeSourceOptionClusterProperties: @{
                    @"sum_r": @[@"sum", @"r"],
                    @"sum_g": @[@"sum", @"g"],
                    @"sum_b": @[@"sum", @"b"],
                    @"count": @[@"count"]
            }
    };

    MLNShapeSource *source = [[MLNShapeSource alloc] initWithIdentifier:@"sensors-source"
                                                               features:features
                                                                options:options];

    MLNStyle *style = self.mapView.style;
    [style addSource:source];

    // === Слой кластеров ===
    MLNCircleStyleLayer *clusterLayer = [[MLNCircleStyleLayer alloc] initWithIdentifier:@"clusters-layer"
                                                                                 source:source];
    clusterLayer.predicate = [NSPredicate predicateWithFormat:@"point_count != nil"];

    // Радиус зависит от количества точек
    clusterLayer.circleRadius = [NSExpression expressionWithFormat:
            @"mgl_step:from:stops:(point_count, 14, %@)",
            @{@10: @18, @50: @24, @100: @30}];

    // Цвет = средний RGB кластера
    clusterLayer.circleColor = [NSExpression expressionWithFormat:
            @"mgl_rgb:(sum_r / count, sum_g / count, sum_b / count)"];
    clusterLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.8];
    clusterLayer.circleStrokeColor = [NSExpression expressionForConstantValue:[UIColor darkGrayColor]];
    clusterLayer.circleStrokeWidth = [NSExpression expressionForConstantValue:@1];

    [style addLayer:clusterLayer];

    // === Слой счётчика в кластере ===
    MLNSymbolStyleLayer *countLayer = [[MLNSymbolStyleLayer alloc] initWithIdentifier:@"clusters-count-layer"
                                                                               source:source];
    countLayer.predicate = [NSPredicate predicateWithFormat:@"point_count != nil"];
    countLayer.text = [NSExpression expressionForKeyPath:@"point_count"];
    countLayer.textFontSize = [NSExpression expressionForConstantValue:@12];
    countLayer.textColor = [NSExpression expressionForConstantValue:[UIColor whiteColor]];
    countLayer.textHaloColor = [NSExpression expressionForConstantValue:[UIColor colorWithWhite:0.27 alpha:1.0]];
    countLayer.textHaloWidth = [NSExpression expressionForConstantValue:@1.5];
    countLayer.textAllowOverlap = [NSExpression expressionForConstantValue:@YES];

    [style addLayer:countLayer];

    // === Слой отдельных маркеров ===
    MLNCircleStyleLayer *markerLayer = [[MLNCircleStyleLayer alloc] initWithIdentifier:@"markers-layer"
                                                                                source:source];
    markerLayer.predicate = [NSPredicate predicateWithFormat:@"point_count == nil"];
    markerLayer.circleRadius = [NSExpression expressionForConstantValue:@11];

    // Конвертируем colorInt в UIColor
    markerLayer.circleColor = [NSExpression expressionWithFormat:
            @"mgl_rgb:(colorInt >> 16 & 255, colorInt >> 8 & 255, colorInt & 255)"];
    markerLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.9];
    markerLayer.circleStrokeColor = [NSExpression expressionForConstantValue:[UIColor darkGrayColor]];
    markerLayer.circleStrokeWidth = [NSExpression expressionForConstantValue:@1];

    [style addLayer:markerLayer];
}

- (void)clearMarkerLayers {
    MLNStyle *style = self.mapView.style;

    // Удаляем слои
    [style removeLayer:[style layerWithIdentifier:@"markers-layer"]];
    [style removeLayer:[style layerWithIdentifier:@"clusters-count-layer"]];
    [style removeLayer:[style layerWithIdentifier:@"clusters-layer"]];

    // Удаляем источник
    [style removeSource:[style sourceWithIdentifier:@"sensors-source"]];
}

- (void)clearMarkers {
    [self clearMarkerLayers];
}

// === Viewport notification ===

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

// === Delegate MLNMapView ===

- (void)mapView:(MLNMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
    [self notifyViewportChanged];
}

- (void)mapViewDidFinishLoadingMap:(MLNMapView *)mapView {
    [self notifyViewportChanged];
}

@end
