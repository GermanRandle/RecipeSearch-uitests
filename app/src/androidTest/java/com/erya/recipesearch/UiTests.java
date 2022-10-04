package com.erya.recipesearch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class UiTests {

    @Rule
    public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void deleteAllSavedIngredients() throws Exception {

        clickMainButton();

        // Need to wait until all photos are loaded and RecyclerView items are created
        Thread.sleep(1000);

        // Delete all saved ingredients
        RecyclerView ingredients = testRule.getActivity()
                .findViewById(com.project.giniatovia.feature_fridge.R.id.rv);
        var needToDelete = Objects.requireNonNull(ingredients.getAdapter()).getItemCount();
        for (int index = needToDelete - 1; index >= 0; index--) {
            onView(withIndex(withId(com.project.giniatovia.feature_fridge.R.id.image_close), index)).perform(click());
        }

        // Return to start screen
        onView(withContentDescription("Go back")).perform(click());
    }

    @Test
    public void cantShowRecipes_IfNoIngredients() throws Exception {

        clickMainButton();

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        typeIngredient("potatoes");

        addIngredient("Potatoes");

        // Wait a little
        Thread.sleep(1000);

        // Remove ingredient
        onView(withId(com.project.giniatovia.feature_fridge.R.id.image_close))
                .perform(click());

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void test2() {

        clickMainButton();
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
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

        // Add ingredient
        onView(withText(ingredient))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click());
    }
}
