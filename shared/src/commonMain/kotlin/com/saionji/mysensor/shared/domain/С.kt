package com.saionji.mysensor.shared.domain

object C {
    const val DASHBOARD_SENSOR_LIMIT = 6
    const val MAP_STYLE_JSON = """
        {
          "version": 8,
          "name": "CARTO Voyager Raster",
          "sources": {
            "carto-voyager": {
              "type": "raster",
              "tiles": [
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
                "https://d.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png"
              ],
              "tileSize": 256,
              "attribution": "&copy; OpenStreetMap contributors &copy; CARTO"
            }
          },
          "layers": [
            {
              "id": "carto-voyager",
              "type": "raster",
              "source": "carto-voyager",
              "minzoom": 0,
              "maxzoom": 19
            }
          ]
        }
    """
}

object MapFonts {
    val DEFAULT = arrayOf("Noto Sans Regular")
}
