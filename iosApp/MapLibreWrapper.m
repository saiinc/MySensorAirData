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

    UITapGestureRecognizer *tapRecognizer =
            [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleMapTap:)];

    for (UIGestureRecognizer *recognizer in self.mapView.gestureRecognizers) {
        if ([recognizer isKindOfClass:[UITapGestureRecognizer class]]) {
            [tapRecognizer requireGestureRecognizerToFail:recognizer];
        }
    }

    [self.mapView addGestureRecognizer:tapRecognizer];

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
                        @"r": [NSNumber numberWithDouble:(double)((colorValue >> 16) & 0xFF)] ?: @0.0,
                        @"g": [NSNumber numberWithDouble:(double)((colorValue >> 8) & 0xFF)] ?: @0.0,
                        @"b": [NSNumber numberWithDouble:(double)(colorValue & 0xFF)] ?: @0.0,
                }
        };
        [geoJSONFeatures addObject:feature];
    }
    if (geoJSONFeatures.count > 0) {
        NSLog(@"DEBUG: First feature properties: %@", geoJSONFeatures.firstObject[@"properties"]);
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

    
    // Если это сработает, в тексте появится максимальный R среди точек кластера
    //NSDictionary *clusterProperties = @{
    //    @"sum_r": @[
    //        [NSExpression expressionWithFormat:@"$featureAccumulated + CAST(r, 'NSNumber')"],
    //        [NSExpression expressionForKeyPath:@"r"]
    //    ],
    //    @"sum_g": @[
    //        [NSExpression expressionWithFormat:@"$featureAccumulated + CAST(g, 'NSNumber')"],
    //        [NSExpression expressionForKeyPath:@"g"]
     //   ],
     //   @"sum_b": @[
     //       [NSExpression expressionWithFormat:@"$featureAccumulated + CAST(b, 'NSNumber')"],
     //       [NSExpression expressionForKeyPath:@"b"]
     //   ]
    //};
    NSDictionary *clusterProperties = @{
        @"sum_r": @[
            [NSExpression expressionWithFormat:@"$featureAccumulated + CAST(r, 'NSNumber')"],
            [NSExpression expressionWithFormat:@"CAST(r, 'NSNumber')"]
        ],
        @"sum_g": @[
            [NSExpression expressionWithFormat:@"$featureAccumulated + CAST(g, 'NSNumber')"],
            [NSExpression expressionWithFormat:@"CAST(g, 'NSNumber')"]
        ],
        @"sum_b": @[
            [NSExpression expressionWithFormat:@"$featureAccumulated + CAST(b, 'NSNumber')"],
            [NSExpression expressionWithFormat:@"CAST(b, 'NSNumber')"]
        ]
    };
    
    //NSDictionary *clusterProperties = @{
    //    @"sum_r": @[
    //        [NSExpression expressionWithFormat:@"$featureAccumulated + TERNARY(r != nil, r, 0)"],
    //        [NSExpression expressionWithFormat:@"TERNARY(r != nil, r, 0)"]
    //    ],
    //    @"sum_g": @[
    //        [NSExpression expressionWithFormat:@"$featureAccumulated + TERNARY(g != nil, g, 0)"],
    //        [NSExpression expressionWithFormat:@"TERNARY(g != nil, g, 0)"]
    //    ],
    //    @"sum_b": @[
    //        [NSExpression expressionWithFormat:@"$featureAccumulated + TERNARY(b != nil, b, 0)"],
    //        [NSExpression expressionWithFormat:@"TERNARY(b != nil, b, 0)"]
    //    ]
    //};


    NSDictionary *options = @{
        MLNShapeSourceOptionClustered: @YES,
        MLNShapeSourceOptionClusterRadius: @40,
        MLNShapeSourceOptionMaximumZoomLevelForClustering: @12,
        //@"clusterProperties": clusterProperties,
        MLNShapeSourceOptionClusterProperties: clusterProperties
    };

    MLNSource *existingSource = [style sourceWithIdentifier:kSensorsSourceId];
    if (existingSource != nil) {
        [self clearMarkerLayers];
    }

    MLNShapeSource *source = [[MLNShapeSource alloc] initWithIdentifier:kSensorsSourceId
                                                                  shape:shape
                                                                options:options];
    [style addSource:source];

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
        

        //clusterLayer.circleColor = [NSExpression expressionWithMLNJSONObject:colorJSON1];
        
        //clusterLayer.circleColor = [NSExpression expressionWithFormat:[UIColor colorWithRed:redVal green:0.55 blue:0.95 alpha:1.0]];
        
        //clusterLayer.circleColor = [NSExpression expressionWithFormat:@"mgl_expressionForRGB(90, 90, 90)"];
        

        // --- 2. Блок расчета канала (с защитой от 0 и Double) ---
        NSArray *(^makeChannelJson)(NSString *) = ^NSArray *(NSString *key) {
            // ВАЖНО: Убираем деление на point_count!
            // Просто берем значение и делим на 255 (или 180 для яркости)
            return @[ @"/",
                       @[ @"to-number", @[ @"get", key ] ],
                     @255.0 ];
        };

        // --- 3. Каскад цветов (Цветовой куб) ---
        // Это ЕДИНСТВЕННЫЙ способ получить честное смешивание каналов в iOS SDK
        NSArray *rJ = makeChannelJson(@"sum_r");
        NSArray *gJ = makeChannelJson(@"sum_g");
        NSArray *bJ = makeChannelJson(@"sum_b");

        UIColor *(^clr)(float, float, float) = ^UIColor *(float r, float g, float b) {
            return [UIColor colorWithRed:r green:g blue:b alpha:1.0];
        };

        NSArray *(^interp)(NSArray *, id, id) = ^NSArray *(NSArray *in, id s0, id s1) {
            return @[@"interpolate", @[@"linear"], in, @0, s0, @1, s1];
        };

        // Собираем куб
        id b00 = interp(bJ, clr(0,0,0), clr(0,0,1));
        id b01 = interp(bJ, clr(0,1,0), clr(0,1,1));
        id b10 = interp(bJ, clr(1,0,0), clr(1,0,1));
        id b11 = interp(bJ, clr(1,1,0), clr(1,1,1));

        id g0 = interp(gJ, b00, b01);
        id g1 = interp(gJ, b10, b11);

        clusterLayer.circleColor = [NSExpression expressionWithMLNJSONObject:interp(rJ, g0, g1)];



        
        //clusterLayer.circleColor = [NSExpression mgl_expressionForInterpolatingExpression:
        //    [NSExpression expressionWithFormat:@"(sum_r + sum_b + sum_g) / 3.0 / point_count"] // Берем средний красный как индекс
        //    withCurveType:MLNExpressionInterpolationModeLinear
        //    parameters:nil
        //    stops:[NSExpression expressionForConstantValue:@{
        //        @0:   [UIColor colorWithRed:0.1 green:0.8 blue:0.1 alpha:1.0], // Зеленый
        //        @127: [UIColor colorWithRed:0.9 green:0.9 blue:0.1 alpha:1.0], // Желтый
        //        @255: [UIColor colorWithRed:0.9 green:0.1 blue:0.1 alpha:1.0]  // Красный
        //    }]];
        

        // Используем MLN/MGL-метод для создания выражения из JSON-массива
        //clusterLayer.circleColor = [NSExpression expressionWithMLNJSONObject:colorComponents];
        
        // используя префикс mgl_, который заставляет парсер работать правильно
        //clusterLayer.circleColor = [NSExpression expressionWithFormat:@"mln_rgb(sum_r / point_count, sum_g / point_count, sum_b / point_count)"];
        //clusterLayer.circleColor = [NSExpression expressionWithFormat:@"RGB(sum_r / point_count, sum_g / point_count, sum_b / point_count)"];
        clusterLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.8];
        clusterLayer.circleStrokeColor = [NSExpression expressionForConstantValue:[UIColor whiteColor]];
        clusterLayer.circleStrokeWidth = [NSExpression expressionForConstantValue:@1];
        [style addLayer:clusterLayer];
    }

    MLNStyleLayer *clusterLayer = [style layerWithIdentifier:kClustersLayerId];

    if ([style layerWithIdentifier:kClustersCountLayerId] == nil) {
        MLNSymbolStyleLayer *countLayer = [[MLNSymbolStyleLayer alloc] initWithIdentifier:kClustersCountLayerId
                                                                                   source:source];
        //countLayer.predicate = [NSPredicate predicateWithFormat:@"cluster == YES"];
        // В слое текстовых меток (MGLSymbolLayer)
        //countLayer.text = [NSExpression expressionWithFormat:@"CAST(sum_g, 'NSString')"];
        //countLayer.text = [NSExpression expressionWithFormat:@"mgl_join({CAST(sum_r/point_count, 'NSString'),' ', CAST(sum_g/point_count, 'NSString')})"];

        countLayer.text = [NSExpression expressionForKeyPath:@"point_count_abbreviated"];
        countLayer.textFontNames = [NSExpression expressionForConstantValue:[self clusterFontNames]];
        countLayer.textFontSize = [NSExpression expressionForConstantValue:@14];
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

- (void)handleMapTap:(UITapGestureRecognizer *)recognizer {
    if (recognizer.state != UIGestureRecognizerStateEnded || self.mapView == nil) {
        return;
    }

    CGPoint point = [recognizer locationInView:self.mapView];

    NSArray<id<MLNFeature>> *features =
            [self.mapView visibleFeaturesAtPoint:point
                    inStyleLayersWithIdentifiers:[NSSet setWithObject:kMarkersLayerId]];

    id<MLNFeature> feature = features.firstObject;
    id markerId = feature.identifier ?: feature.attributes[@"id"];

    if (markerId != nil) {
        if (self.onMarkerClick) {
            self.onMarkerClick([markerId description]);
        }
        return;
    }

    if (self.onMapClick) {
        self.onMapClick();
    }
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
