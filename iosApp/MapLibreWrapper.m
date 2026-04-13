//
//  MapLibreWrapper.m
//  iosApp
//
//  Created by Saionji on 08.04.2026.
//

#import "MapLibreWrapper.h"
#import <MapLibre/MapLibre.h>
#import <CoreLocation/CoreLocation.h>

static NSString * const kSensorsSourceId = @"sensors-source";
static NSString * const kClustersLayerId = @"clusters-layer";
static NSString * const kClustersCountLayerId = @"clusters-count-layer";
static NSString * const kMarkersLayerId = @"markers-layer";

@interface MapLibreWrapper ()
@property (nonatomic, strong) MLNMapView *mapView;
@end

@implementation MapLibreWrapper

- (NSArray<NSString *> *)clusterFontNames {
    return @[@"Noto Sans Regular"];
}

- (NSString *)hexColorStringFromColorInt:(NSNumber *)colorIntNumber {
    int colorInt = [colorIntNumber intValue];
    int r = (colorInt >> 16) & 0xFF;
    int g = (colorInt >> 8) & 0xFF;
    int b = colorInt & 0xFF;
    return [NSString stringWithFormat:@"#%02X%02X%02X", r, g, b];
}

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
    MLNStyle *style = self.mapView.style;
    if (style == nil) {
        return;
    }

    if ([markers count] == 0) {
        [self clearMarkers];
        return;
    }

    // Создаём GeoJSON FeatureCollection. Для кластеризации в iOS MapLibre этот путь
    // надежнее, чем вручную собранная коллекция MLNPointFeature.
    NSMutableArray<NSDictionary *> *geoJSONFeatures = [NSMutableArray array];
    for (NSDictionary *marker in markers) {
        double lat = [marker[@"lat"] doubleValue];
        double lon = [marker[@"lon"] doubleValue];
        NSNumber *colorInt = marker[@"colorInt"] ?: @0;
        int colorValue = [colorInt intValue];
        NSDictionary *feature = @{
                @"type": @"Feature",
                @"geometry": @{
                        @"type": @"Point",
                        @"coordinates": @[@(lon), @(lat)]
                },
                @"properties": @{
                        @"id": marker[@"id"] ?: @"",
                        @"colorInt": colorInt,
                        @"color": [self hexColorStringFromColorInt:colorInt],
                        @"r": @((colorValue >> 16) & 0xFF),
                        @"g": @((colorValue >> 8) & 0xFF),
                        @"b": @(colorValue & 0xFF)
                }
        };
        [geoJSONFeatures addObject:feature];
    }

    NSDictionary *featureCollection = @{
            @"type": @"FeatureCollection",
            @"features": geoJSONFeatures
    };

    NSError *jsonError = nil;
    NSData *geoJSONData = [NSJSONSerialization dataWithJSONObject:featureCollection options:0 error:&jsonError];
    if (geoJSONData == nil) {
        NSLog(@"Failed to serialize marker GeoJSON: %@", jsonError);
        return;
    }

    NSError *shapeError = nil;
    MLNShape *shape = [MLNShape shapeWithData:geoJSONData
                                     encoding:NSUTF8StringEncoding
                                        error:&shapeError];
    if (shape == nil) {
        NSLog(@"Failed to parse marker GeoJSON into MLNShape: %@", shapeError);
        return;
    }

    NSDictionary *clusterProperties = @{
        @"sum_r": @[
            [NSExpression expressionWithFormat:@"sum:({$featureAccumulated, sum_r})"],
            [NSExpression expressionForKeyPath:@"r"]
        ],
        @"sum_g": @[
            [NSExpression expressionWithFormat:@"sum:({$featureAccumulated, sum_g})"],
            [NSExpression expressionForKeyPath:@"g"]
        ],
        @"sum_b": @[
            [NSExpression expressionWithFormat:@"sum:({$featureAccumulated, sum_b})"],
            [NSExpression expressionForKeyPath:@"b"]
        ]
    };

    NSDictionary *options = @{
        MLNShapeSourceOptionClustered: @YES,
        MLNShapeSourceOptionClusterRadius: @40,
        MLNShapeSourceOptionMaximumZoomLevelForClustering: @12,
        MLNShapeSourceOptionClusterProperties: clusterProperties
    };

    MLNShapeSource *source = (MLNShapeSource *)[style sourceWithIdentifier:kSensorsSourceId];
    if (source == nil) {
        source = [[MLNShapeSource alloc] initWithIdentifier:kSensorsSourceId
                                                      shape:shape
                                                    options:options];
        [style addSource:source];
    } else {
        source.shape = shape;
    }

    if ([style layerWithIdentifier:kClustersLayerId] == nil) {
        MLNCircleStyleLayer *clusterLayer = [[MLNCircleStyleLayer alloc] initWithIdentifier:kClustersLayerId
                                                                                     source:source];
        clusterLayer.predicate = [NSPredicate predicateWithFormat:@"cluster == YES"];
        clusterLayer.circleRadius = [NSExpression mgl_expressionForSteppingExpression:
                                     [NSExpression expressionForKeyPath:@"point_count"]
                                                                             fromExpression:[NSExpression expressionForConstantValue:@14]
                                                                                      stops:[NSExpression expressionForConstantValue:@{
                                                                                          @10: @18,
                                                                                          @50: @24
                                                                                      }]];
        clusterLayer.circleColor = [NSExpression expressionForConstantValue:[UIColor colorWithRed:0.12 green:0.55 blue:0.95 alpha:1.0]];
        clusterLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.8];
        clusterLayer.circleStrokeColor = [NSExpression expressionForConstantValue:[UIColor darkGrayColor]];
        clusterLayer.circleStrokeWidth = [NSExpression expressionForConstantValue:@1];
        [style addLayer:clusterLayer];
    }

    MLNStyleLayer *clusterLayer = [style layerWithIdentifier:kClustersLayerId];

    if ([style layerWithIdentifier:kClustersCountLayerId] == nil) {
        MLNSymbolStyleLayer *countLayer = [[MLNSymbolStyleLayer alloc] initWithIdentifier:kClustersCountLayerId
                                                                                   source:source];
        countLayer.predicate = [NSPredicate predicateWithFormat:@"cluster == YES"];
        countLayer.text = [NSExpression expressionForKeyPath:@"point_count_abbreviated"];
        countLayer.textFontNames = [NSExpression expressionForConstantValue:[self clusterFontNames]];
        countLayer.textFontSize = [NSExpression expressionForConstantValue:@12];
        countLayer.textColor = [NSExpression expressionForConstantValue:[UIColor whiteColor]];
        countLayer.textHaloColor = [NSExpression expressionForConstantValue:[UIColor colorWithWhite:0.27 alpha:1.0]];
        countLayer.textHaloWidth = [NSExpression expressionForConstantValue:@1.5];
        countLayer.textAllowsOverlap = [NSExpression expressionForConstantValue:@YES];
        countLayer.textIgnoresPlacement = [NSExpression expressionForConstantValue:@YES];
        [style insertLayer:countLayer aboveLayer:clusterLayer];
    }

    MLNStyleLayer *countLayer = [style layerWithIdentifier:kClustersCountLayerId];

    if ([style layerWithIdentifier:kMarkersLayerId] == nil) {
        MLNCircleStyleLayer *markerLayer = [[MLNCircleStyleLayer alloc] initWithIdentifier:kMarkersLayerId
                                                                                    source:source];
        markerLayer.predicate = [NSPredicate predicateWithFormat:@"cluster != YES"];
        markerLayer.circleRadius = [NSExpression expressionForConstantValue:@11];
        markerLayer.circleColor = [NSExpression expressionForKeyPath:@"color"];
        markerLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.9];
        markerLayer.circleStrokeColor = [NSExpression expressionForConstantValue:[UIColor darkGrayColor]];
        markerLayer.circleStrokeWidth = [NSExpression expressionForConstantValue:@1];

        if (countLayer != nil) {
            [style insertLayer:markerLayer belowLayer:countLayer];
        } else {
            [style addLayer:markerLayer];
        }
    }
}

- (void)clearMarkerLayers {
    MLNStyle *style = self.mapView.style;
    if (style == nil) return;

    // Удаляем слои с проверкой на nil
    MLNStyleLayer *markersLayer = [style layerWithIdentifier:kMarkersLayerId];
    if (markersLayer) [style removeLayer:markersLayer];

    MLNStyleLayer *countLayer = [style layerWithIdentifier:kClustersCountLayerId];
    if (countLayer) [style removeLayer:countLayer];

    MLNStyleLayer *clusterLayer = [style layerWithIdentifier:kClustersLayerId];
    if (clusterLayer) [style removeLayer:clusterLayer];

    // Удаляем источник с проверкой на nil
    MLNSource *source = [style sourceWithIdentifier:kSensorsSourceId];
    if (source) [style removeSource:source];
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
