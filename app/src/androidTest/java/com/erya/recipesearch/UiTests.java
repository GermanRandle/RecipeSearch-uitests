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

    private static final String LOG_TAG = "UI_TEST";

    @Rule
    public ActivityTestRule<MainActivity> testMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void deleteAllSavedInfo() {

        clickMainButton();

        // Need to wait until all photos are loaded and RecyclerView items are created
        waitForApi();

        // Delete all saved products
        RecyclerView products = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_fridge.R.id.rv);
        int productsToDelete = Objects.requireNonNull(products.getAdapter()).getItemCount();
        for (int index = productsToDelete - 1; index >= 0; index--) {
            onView(withIndex(withId(com.project.giniatovia.feature_fridge.R.id.image_close),
                            index)).perform(click());
        }

        // Back to start screen
        previousScreen();

        // Delete all saved recipes
        onView(withId(R.id.secondary_btn))
                .perform(click());
        RecyclerView savedRecipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        int favoritesCount = Objects.requireNonNull(savedRecipes.getAdapter()).getItemCount();
        for (int index = favoritesCount - 1; index >= 0; index--) {
            onView(withIndex(withId(com.project.giniatovia.feature_fridge.R.id.product_image),
                            index)).perform(click());
            waitForApi();
            onView(withId(com.project.giniatovia.feature_recipe.R.id.bookmark))
                    .perform(click());
            previousScreen();
        }

        // Back to start screen
        previousScreen();
    }

    @Test
    public void cantShowRecipesIfNoProducts() {

        clickMainButton();

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        addProduct("Potatoes");

        // Wait for product to load
        waitForApi();

        // Remove product
        onView(withId(com.project.giniatovia.feature_fridge.R.id.image_close))
                .perform(click());

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void saveRecipeCheckSaveThenRemove() {

        addSomeRecipeToFavorites();

        // Back to start screen
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
        waitForApi();
        onView(withId(com.project.giniatovia.feature_recipe.R.id.bookmark))
                .perform(click());

        // Return back and check that there are no saved recipes left
        previousScreen();
        savedRecipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        favoritesCount = Objects.requireNonNull(savedRecipes.getAdapter()).getItemCount();
        Assert.assertEquals(0, favoritesCount);
    }

    @Test
    public void addToFavoritesThenImmediatelyRemove() {

        /*
         This test should fail because it's a bug in our application :)
         */

        addSomeRecipeToFavorites();

        // Remove from favorites
        onView(withId(com.project.giniatovia.feature_recipe.R.id.bookmark))
                .perform(click());

        // Return to start screen
        previousScreen();
        previousScreen();
        previousScreen();

        // Check whether there are no saved recipes
        onView(withId(R.id.secondary_btn))
                .perform(click());
        RecyclerView savedRecipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        int favoritesCount = Objects.requireNonNull(savedRecipes.getAdapter()).getItemCount();
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

    private void addSomeRecipeToFavorites() {

        clickMainButton();

        // Add some products
        addProduct("Apple");
        addProduct("Orange");
        addProduct("Banana");

        // Show recipes and wait for RecyclerView to appear
        showRecipes();
        var recyclerView = onView(withId(com.project.giniatovia.feature_recipe.R.id.rv_recipe));
        RecyclerView recipes = testMainActivityRule.getActivity()
                .findViewById(com.project.giniatovia.feature_recipe.R.id.rv_recipe);
        waitForApi();

        // Choose random recipe
        var recipesFound = Objects.requireNonNull(recipes.getAdapter()).getItemCount();
        Assert.assertTrue(recipesFound > 0);
        // TODO: support RecyclerView scrolling
        var randomPos = new Random().nextInt(Math.min(recipesFound, 4));
        Log.i(LOG_TAG, "addRecipeToFavorites: random recipe position: " + randomPos);

        // Click on recipe and wait for recipe to appear
        recyclerView.perform(actionOnItemAtPosition(randomPos, click()));
        waitForApi();

        // Add to favorites
        // TODO: check that bookmark icon changed color
        onView(withId(com.project.giniatovia.feature_recipe.R.id.bookmark))
                .perform(click());
    }

    private void clickMainButton() {

        // Clicking "Choose a new dish"
        onView(withId(R.id.main_btn))
                .perform(click());
    }

    private void addProduct(String product) {

        // Type product in AutoCompleteTextView
        onView(withId(com.project.giniatovia.feature_fridge.R.id.autoCompleteTextView))
                .perform(click())
                .perform(typeText(product));

        // Add product
        onView(withText(product))
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

    private void waitForApi() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Thread.sleep() was interrupted: " + e.getMessage());
        }
    }
}
