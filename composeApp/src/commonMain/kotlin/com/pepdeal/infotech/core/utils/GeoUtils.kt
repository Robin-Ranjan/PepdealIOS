package com.pepdeal.infotech.core.utils

import kotlin.math.*

object GeoUtils {

    // Base32 map for geohash encoding
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    // Geohash encoding
    fun encodeGeohash(latitude: Double, longitude: Double, precision: Int = 6): String {
        var latRange = -90.0 to 90.0
        var lngRange = -180.0 to 180.0
        var isEven = true
        var bit = 0
        var ch = 0
        val geohash = StringBuilder()

        while (geohash.length < precision) {
            val mid = if (isEven) (lngRange.first + lngRange.second) / 2 else (latRange.first + latRange.second) / 2

            if (isEven) {
                if (longitude > mid) {
                    ch = ch or (1 shl (4 - bit))
                    lngRange = mid to lngRange.second
                } else {
                    lngRange = lngRange.first to mid
                }
            } else {
                if (latitude > mid) {
                    ch = ch or (1 shl (4 - bit))
                    latRange = mid to latRange.second
                } else {
                    latRange = latRange.first to mid
                }
            }

            isEven = !isEven

            if (++bit == 5) {
                geohash.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }

        return geohash.toString()
    }

    // Get 8 neighbors of a geohash (you can extend to 24 for bigger areas)
    fun getGeohashNeighbors(hash: String): List<String> {
        val neighbors = mutableListOf<String>()
        val latLng = decodeGeohashCenter(hash)
        val lat = latLng.first
        val lng = latLng.second

        val delta = 0.005 // ~550m, adjust as needed for precision 6
        for (dLat in -1..1) {
            for (dLng in -1..1) {
                if (dLat == 0 && dLng == 0) continue
                neighbors += encodeGeohash(lat + dLat * delta, lng + dLng * delta, hash.length)
            }
        }

        return neighbors.distinct()
    }

    // Decode geohash to center point (approximate, used for neighbor calc)
    private fun decodeGeohashCenter(hash: String): Pair<Double, Double> {
        var latRange = -90.0 to 90.0
        var lngRange = -180.0 to 180.0
        var isEven = true

        for (c in hash) {
            val cd = BASE32.indexOf(c)
            for (mask in listOf(16, 8, 4, 2, 1)) {
                if (isEven) {
                    val mid = (lngRange.first + lngRange.second) / 2
                    lngRange = if (cd and mask != 0) mid to lngRange.second else lngRange.first to mid
                } else {
                    val mid = (latRange.first + latRange.second) / 2
                    latRange = if (cd and mask != 0) mid to latRange.second else latRange.first to mid
                }
                isEven = !isEven
            }
        }

        val lat = (latRange.first + latRange.second) / 2
        val lng = (lngRange.first + lngRange.second) / 2
        return lat to lng
    }

    // Haversine distance in kilometers
    private fun haversineDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val R = 6371.0 // Earth radius in kilometers

        val dLat = (lat2 - lat1).toRadians()
        val dLng = (lng2 - lng1).toRadians()

        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun Double.toRadians(): Double = this * PI / 180

    fun isWithinRadiusKm(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
        radiusKm: Double
    ): Boolean {
        return haversineDistance(lat1, lng1, lat2, lng2) <= radiusKm
    }
}
