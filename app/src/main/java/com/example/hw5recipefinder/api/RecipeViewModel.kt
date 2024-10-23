package com.example.hw5recipefinder.api


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Repository to interact with the API
class RecipeRepository(private val apiService: SpoonacularApiService) {

    suspend fun searchRecipes(query: String, diet: String?, cuisine: String?, maxCalories: Int?): List<Recipe> {
        // Call the API, passing nulls where the user didn't provide input
        return apiService.searchRecipes(query, diet, cuisine, maxCalories).results
    }

    suspend fun getRecipeDetails(id: Int): Recipe {
        return apiService.getRecipeDetails(id)
    }
}


// ViewModel to handle UI logic and data
class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    // StateFlow to hold the list of recipes
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _recipeDetails = MutableStateFlow<Recipe?>(null)
    val recipeDetails: StateFlow<Recipe?> = _recipeDetails

    fun getRecipeDetails(id: Int) {
        viewModelScope.launch {
            _recipeDetails.value = repository.getRecipeDetails(id)
        }
    }
    // Function to search for recipes
    fun searchRecipes(query: String, diet: String?, cuisine: String?, maxCalories: Int?) {
        viewModelScope.launch {
            _recipes.value = repository.searchRecipes(query, diet, cuisine, maxCalories)
        }
    }
}

// ViewModel Factory to provide the repository to the ViewModel
class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}