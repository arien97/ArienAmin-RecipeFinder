package com.example.hw5recipefinder

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import coil3.compose.AsyncImagePainter
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
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    var query by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("") }
    var diet by remember { mutableStateOf("") }
    var maxCalories by remember { mutableStateOf("") }
    val recipes by viewModel.recipes.collectAsState()

    if (isPortrait) {
        Column(modifier = Modifier.padding(16.dp)) {
            RecipeSearchFields(query, cuisine, diet, maxCalories, onQueryChange = {
                query = it
            }, onCuisineChange = {
                cuisine = it
            }, onDietChange = {
                if (it != null) {
                    diet = it
                }
            }, onMaxCaloriesChange = {
                maxCalories = it
            }, viewModel, navController)

            Spacer(modifier = Modifier.height(16.dp))

            // Display search results in portrait mode
            RecipeListScreen(recipes, navController)
        }
    } else {
        Row(modifier = Modifier.padding(16.dp)) {
            // Search bar on the left
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                RecipeSearchFields(query, cuisine, diet, maxCalories, onQueryChange = {
                    query = it
                }, onCuisineChange = {
                    cuisine = it
                }, onDietChange = {
                    if (it != null) {
                        diet = it
                    }
                }, onMaxCaloriesChange = {
                    maxCalories = it
                }, viewModel, navController)
            }
            Column(modifier = Modifier.weight(2f)) {
                // Display search results in landscape mode
                RecipeListScreen(recipes, navController)
            }
        }
    }
}


@Composable
fun RecipeSearchFields(
    query: String,
    cuisine: String,
    diet: String?,
    maxCalories: String,
    onQueryChange: (String) -> Unit,
    onCuisineChange: (String) -> Unit,
    onDietChange: (String?) -> Unit,
    onMaxCaloriesChange: (String) -> Unit,
    viewModel: RecipeViewModel,
    navController: NavController
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search recipes") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = cuisine,
        onValueChange = onCuisineChange,
        label = { Text("Cuisine (optional)") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))
    DietDropdownMenu(selectedDiet = diet, onDietChange = onDietChange)
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = maxCalories,
        onValueChange = onMaxCaloriesChange,
        label = { Text("Max Calories (optional)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = {
        val calories = maxCalories.toIntOrNull()
        viewModel.searchRecipes(query, diet, cuisine, calories)
        onDietChange(null) // Reset the diet selection after search
    }) {
        Text("Search")
    }
}


@Composable
fun RecipeList(recipes: List<Recipe>) {
    Column {
        for (recipe in recipes) {
            RecipeItem(recipe)
            HorizontalDivider()
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
        ImageFromUrl(recipe.image)

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
fun ImageFromUrl(imageUrl: String, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(model = imageUrl)
    val painterState by painter.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        when (painterState) {
            is AsyncImagePainter.State.Loading -> {
                // Display a loading indicator
                Text("Loading...")
                Log.d("ImageFromUrl", "Loading image from URL: $imageUrl")
            }
            is AsyncImagePainter.State.Error -> {
                // Display an error message
                Text("Error loading image")
                Log.e("ImageFromUrl", "Error loading image from URL: $imageUrl")
                val errorState = painterState as AsyncImagePainter.State.Error
                Log.e("ImageFromUrl", "Error: ${errorState.result.throwable}")
            }
            is AsyncImagePainter.State.Success -> {
                // Image loaded successfully
                Log.d("ImageFromUrl", "Successfully loaded image from URL: $imageUrl")
            }
            else -> {
                // Handle other states if necessary
            }
        }
    }
}

@Composable
fun RecipeDetailsScreen(
    recipeId: Int,
    viewModel: RecipeViewModel,
    navController: NavController
) {
    val recipe by viewModel.recipeDetails.collectAsState()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    LaunchedEffect(recipeId) {
        Log.d("RecipeDetailsScreen", "Fetching details for recipe ID: $recipeId")
        viewModel.getRecipeDetails(recipeId)
    }

    if (recipe == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        recipe?.let { recipeDetails ->
            if (isPortrait) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    RecipeDetailsContent(recipeDetails)
                    Spacer(modifier = Modifier.height(60.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(text = recipeDetails.title, style = MaterialTheme.typography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                ImageFromUrl(recipeDetails.image)
                            }
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Ingredients", style = MaterialTheme.typography.displaySmall)
                                recipeDetails.extendedIngredients.forEach { ingredient ->
                                    Text(text = "${ingredient.name}: ${ingredient.amount} ${ingredient.unit}")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Instructions", style = MaterialTheme.typography.displaySmall)
                                Text(recipeDetails.instructions ?: "Instructions not available")
                            }
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(text = "Back to Search")
                }
            }
        }
    }
}

@Composable
fun RecipeDetailsContent(recipeDetails: Recipe) {
    Text(text = recipeDetails.title, style = MaterialTheme.typography.displayLarge)
    Spacer(modifier = Modifier.height(8.dp))
    ImageFromUrl(recipeDetails.image)
    Spacer(modifier = Modifier.height(8.dp))

    Text(text = "Ingredients", style = MaterialTheme.typography.displaySmall)
    recipeDetails.extendedIngredients.forEach { ingredient ->
        Text(text = "${ingredient.name}: ${ingredient.amount} ${ingredient.unit}")
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(text = "Instructions", style = MaterialTheme.typography.displaySmall)
    Text(recipeDetails.instructions ?: "Instructions not available")
}

@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val columns = if (screenWidth > 600) 2 else 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(recipes) { recipe ->
            RecipeItem(recipe = recipe, onClick = {
                navController.navigate("recipeDetails/${recipe.id}")
            })
        }
    }
}

@Composable
fun DietDropdownMenu(selectedDiet: String?, onDietChange: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val dietOptions = listOf(
        "Gluten Free", "Ketogenic", "Vegetarian", "Lacto-Vegetarian", "Ovo-Vegetarian",
        "Vegan", "Pescetarian", "Paleo", "Primal", "Low FODMAP", "Whole30"
    )

    Box {
        TextField(
            value = selectedDiet ?: "",
            onValueChange = { /* No-op since it's read-only */ },
            label = { Text("Diet (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = true }
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dietOptions.forEach { diet ->
                DropdownMenuItem(
                    text = { Text(text = diet) },
                    onClick = {
                        onDietChange(diet)
                        expanded = false
                    }
                )
            }
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
            ImageFromUrl(recipe.image)
            Text(text = recipe.title, style = MaterialTheme.typography.displayMedium)
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

            RecipeDetailsScreen(
                recipeId = recipeId ?: 0,
                viewModel = recipeViewModel,
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
