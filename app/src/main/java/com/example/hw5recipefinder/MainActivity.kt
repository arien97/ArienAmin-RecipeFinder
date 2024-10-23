package com.example.hw5recipefinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.hw5recipefinder.api.Recipe
import com.example.hw5recipefinder.api.RecipeViewModel
import com.example.hw5recipefinder.ui.theme.HW5RecipeFinderTheme
import com.example.hw5recipefinder.api.RecipeRepository
import com.example.hw5recipefinder.api.RetrofitInstance
import com.example.hw5recipefinder.api.RecipeViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<RecipeViewModel> {
        RecipeViewModelFactory(RecipeRepository(RetrofitInstance.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HW5RecipeFinderTheme {
                RecipeApp(viewModel)
            }
        }
    }
}

@Composable
fun RecipeSearchScreen(viewModel: RecipeViewModel, navController: NavController) {
    var query by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("") }
    var diet by remember { mutableStateOf("") }
    var maxCalories by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        // Search query input
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search recipes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cuisine input
        TextField(
            value = cuisine,
            onValueChange = { cuisine = it },
            label = { Text("Cuisine (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Diet input
        TextField(
            value = diet,
            onValueChange = { diet = it },
            label = { Text("Diet (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Max calories input
        TextField(
            value = maxCalories,
            onValueChange = { maxCalories = it },
            label = { Text("Max Calories (optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search button
        Button(onClick = {
            val calories = maxCalories.toIntOrNull()
            viewModel.searchRecipes(query, diet, cuisine, calories)
        }) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the search results
        val recipes by viewModel.recipes.collectAsState()
        LazyColumn {
            items(recipes) { recipe ->
                RecipeItem(recipe = recipe, onClick = {
                    navController.navigate("recipeDetails/${recipe.id}")
                })
            }
        }
    }
}


// Composable function to display a list of recipes
@Composable
fun RecipeList(recipes: List<Recipe>) {
    Column {
        for (recipe in recipes) {
            RecipeItem(recipe)
            Divider()
        }
    }
}

// Composable function to display a single recipe
@Composable
fun RecipeItem(recipe: Recipe) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = recipe.image,
            contentDescription = "Recipe Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop

        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(recipe.title, style = MaterialTheme.typography.bodyLarge)

            // Display ingredients if available, otherwise show a placeholder
            if (recipe.extendedIngredients.isNotEmpty()) {
                Text(
                    "Ingredients: ${recipe.extendedIngredients.joinToString(", ") { it.name }}",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text("Ingredients not available", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun RecipeDetailsScreen(
    recipeId: Int,  // Fetch recipe by ID, if needed
    recipe: Recipe?,
    navController: NavController
) {
    // Use a Box layout to allow the button to stay fixed at the bottom
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            recipe?.let {
                Text(text = it.title, style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = recipe.image)
                Image(
                    painter = rememberAsyncImagePainter(recipe.image),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
                // Recipe Image
                AsyncImage(
                    model = recipe.image,
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop

                )
                Spacer(modifier = Modifier.height(8.dp))

                // Ingredients
                Text(text = "Ingredients", style = MaterialTheme.typography.displayMedium)
                it.extendedIngredients.forEach { ingredient ->
                    Text(text = "${ingredient.name}: ${ingredient.amount} ${ingredient.unit}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Instructions
                Text(text = "Instructions", style = MaterialTheme.typography.bodyMedium)
                it.instructions?.let { instructions ->
                    Text(text = instructions)
                } ?: Text("Instructions not available")

                Spacer(modifier = Modifier.height(16.dp))
            } ?: run {
                Text(text = "Recipe details not available")
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Fixed Back to Search button at the bottom
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Back to Search")
        }
    }
}

@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    navController: NavController
) {
    LazyColumn {
        items(recipes) { recipe ->
            RecipeItem(recipe = recipe, onClick = {
                navController.navigate("recipeDetails/${recipe.id}")
            })
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            AsyncImage(
                model = recipe.image,
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop

            )
            Text(text = recipe.title, style = MaterialTheme.typography.displayLarge)
        }
    }
}

@Composable
fun RecipeApp(recipeViewModel: RecipeViewModel) {
    // Create NavController
    val navController = rememberNavController()

    // Collect the recipe list as state from the ViewModel's StateFlow
    val recipes by recipeViewModel.recipes.collectAsState()

    // Setup NavHost with routes
    NavHost(
        navController = navController,
        startDestination = "recipeSearch"
    ) {
        // Recipe search route
        composable("recipeSearch") {
            RecipeSearchScreen(viewModel = recipeViewModel, navController = navController)
        }

        // Recipe list route
        composable("recipeList") {
            RecipeListScreen(
                recipes = recipes,  // Now this is a List<Recipe>
                navController = navController
            )
        }

        // Recipe details route
        composable("recipeDetails/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toInt()

            // Find the recipe using collectAsState to observe the list
            val recipe = recipes.find { it.id == recipeId }

            RecipeDetailsScreen(
                recipeId = recipeId ?: 0,
                recipe = recipe,
                navController = navController
            )
        }
    }
}

// Preview for Compose UI
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HW5RecipeFinderTheme {
    }
}
