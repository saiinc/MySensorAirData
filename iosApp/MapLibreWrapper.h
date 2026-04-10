//
//  Header.h
//  iosApp
//
//  Created by Saionji on 08.04.2026.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MapLibreWrapper : NSObject

- (UIView *)createMapView;
- (void)moveTo:(double)lat lon:(double)lon zoom:(double)zoom;
- (void)zoomIn;
- (void)zoomOut;
- (double)getZoom;
- (double)getCenterLat;
- (double)getCenterLon;

@end

NS_ASSUME_NONNULL_END
