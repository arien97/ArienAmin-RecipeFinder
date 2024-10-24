//package com.example.hw5recipefinder.api
//
//import android.app.Application
//import coil3.ImageLoader
//import coil3.SingletonImageLoader
////import coil3.ImageLoaderFactory
//import coil3.annotation.ExperimentalCoilApi
//import coil3.disk.DiskCache
//import coil3.memory.MemoryCache
//import coil3.util.DebugLogger
//
//abstract class GreenApplication : Application(), SingletonImageLoader.Factory {
//    @OptIn(ExperimentalCoilApi::class)
//    override fun newImageLoader(): ImageLoader {
//        return ImageLoader.Builder(this)
//            .memoryCache {
//                MemoryCache.Builder(this)
//                    .maxSizePercent(0.20)
//                    .build()
//            }
//            .diskCache {
//                DiskCache.Builder()
//                    .directory(cacheDir.resolve("image_cache"))
//                    .maxSizeBytes(5 * 1024 * 1024)
//                    .build()
//            }
//            .logger(DebugLogger())
//            .respectCacheHeaders(false)
//            .build()
//    }
//}
