package com.example.hw5recipefinder.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Recipe data class for search results and details
data class Recipe(
    val id: Int,
    val title: String,
    val image: String,
    val summary: String? = null,
    val instructions: String? = null,
    val extendedIngredients: List<Ingredient> = emptyList()  // Default to an empty list
)

data class Ingredient(
    val id: Int,
    val name: String,
    val amount: Double,
    val unit: String
)

// API service interface
interface SpoonacularApiService {

    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("diet") diet: String?,            // Nullable diet
        @Query("cuisine") cuisine: String?,      // Nullable cuisine
        @Query("maxCalories") maxCalories: Int?, // Nullable max calories
        @Query("number") number: Int = 10,       // Default number of results
        @Query("instructionsRequired") instructionsRequired: Boolean = true,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true,
        @Query("fillIngredients") fillIngredients: Boolean = true,
        @Query("addRecipeNutrition") addRecipeNutrition: Boolean = true,
        @Query("apiKey") apiKey: String = "22a0427b740a4ea99b53fd17e6e70c2c"
    ): RecipeSearchResponse

    @GET("recipes/{id}/information")
    suspend fun getRecipeDetails(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String = "22a0427b740a4ea99b53fd17e6e70c2c"
    ): Recipe
}

// Response class to map the list of recipes from the API
data class RecipeSearchResponse(val results: List<Recipe>)

// Singleton Retrofit instance
object RetrofitInstance {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    val api: SpoonacularApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpoonacularApiService::class.java)
    }
}