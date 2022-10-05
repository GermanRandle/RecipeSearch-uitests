package com.erya.recipesearch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.erya.recipesearch.presentation.activity.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class UiTests {

    // For how long we wait for Spoonacular API to react and views to create
    private static final int WAIT_TIME = 2000;

    @Rule
    public ActivityTestRule<MainActivity> testMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void deleteAllSavedIngredients() throws Exception {

        // TODO: delete all favorite recipes too

        clickMainButton();

        // Need to wait until all photos are loaded and RecyclerView items are created
        Thread.sleep(WAIT_TIME);

        // Delete all saved ingredients
        RecyclerView ingredients = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_fridge.R.id.rv);
        var needToDelete = Objects.requireNonNull(ingredients.getAdapter()).getItemCount();
        for (int index = needToDelete - 1; index >= 0; index--) {
            onView(withIndex(withId(com.project.giniatovia.feature_fridge.R.id.image_close), index)).perform(click());
        }

        previousScreen();
    }

    @Test
    public void cantShowRecipesIfNoIngredients() throws Exception {

        clickMainButton();

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        addIngredient("Potatoes");

        // Wait a little
        Thread.sleep(WAIT_TIME);

        // Remove ingredient
        onView(withId(com.project.giniatovia.feature_fridge.R.id.image_close))
                .perform(click());

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void addRecipeToFavoritesThenRemove() throws Exception {

        clickMainButton();

        // Add some ingredients
        addIngredient("Apple");
        addIngredient("Orange");
        addIngredient("Banana");

        // Show recipes and wait for RecyclerView to appear
        showRecipes();
        var recyclerView = onView(withId(com.project.giniatovia.feature_recipe.R.id.rv_recipe));
        RecyclerView recipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        Thread.sleep(WAIT_TIME);

        // Choose random recipe
        var recipesFound = Objects.requireNonNull(recipes.getAdapter()).getItemCount();
        Assert.assertTrue(recipesFound > 0);
        // TODO: support RecyclerView scrolling
        var randomPos = new Random().nextInt(Math.min(recipesFound, 4));
        Log.i("TEST", "addRecipeToFavorites: random recipe position: " + randomPos);

        // Click on recipe and wait for recipe to appear
        recyclerView.perform(actionOnItemAtPosition(randomPos, click()));
        Thread.sleep(WAIT_TIME);

        // Add to favorites
        // TODO: check that bookmark icon changed color
        onView(withId(com.project.giniatovia.feature_recipe.R.id.bookmark))
                .perform(click());

        // Return to start screen
        previousScreen();
        previousScreen();
        previousScreen();

        // See saved recipes
        onView(withId(R.id.secondary_btn))
                .perform(click());

        // Remove recipe from favorites
        RecyclerView savedRecipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        int favoritesCount = Objects.requireNonNull(savedRecipes.getAdapter()).getItemCount();
        Assert.assertEquals(1, favoritesCount);
        onView(withId(com.project.giniatovia.feature_recipe.R.id.product_image))
                .perform(click());
        Thread.sleep(WAIT_TIME);
        onView(withId(com.project.giniatovia.feature_recipe.R.id.bookmark))
                .perform(click());

        // Return back and check that there are no saved recipes left
        previousScreen();
        savedRecipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        favoritesCount = Objects.requireNonNull(savedRecipes.getAdapter()).getItemCount();
        Assert.assertEquals(0, favoritesCount);
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<>() {
            int curIndex = 0;

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && curIndex++ == index;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }
        };
    }

    private void clickMainButton() {

        // Clicking "Choose a new dish"
        onView(withId(R.id.main_btn))
                .perform(click());
    }

    private void typeIngredient(String text) {

        // Type ingredient in AutoCompleteTextView
        onView(withId(com.project.giniatovia.feature_fridge.R.id.autoCompleteTextView))
                .perform(click())
                .perform(typeText(text));
    }

    private void addIngredient(String ingredient) {

        typeIngredient(ingredient);

        // Add ingredient
        onView(withText(ingredient))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click());
    }

    private void showRecipes() {

        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .perform(click());
    }

    private void previousScreen() {

        // Click arrow to go back
        // TODO: I know checking contentDescription like that is not good but
        //  on some screens arrow is added in Kotlin code and thus don't appear in R.id
        onView(withContentDescription("Go back")).perform(click());
    }
}
