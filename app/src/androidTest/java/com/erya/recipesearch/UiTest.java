package com.erya.recipesearch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.erya.recipesearch.presentation.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UiTest {

    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void cantShowRecipes_IfNoIngredients() throws Exception {

        // Clicking "Choose a new dish"
        onView(withId(R.id.main_btn))
                .perform(click());

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // Type ingredient in AutoCompleteTextView
        onView(withId(com.project.giniatovia.feature_fridge.R.id.autoCompleteTextView))
                .perform(click())
                .perform(typeText("potatoes"));

        // Add ingredient
        onView(withText("Potatoes"))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click());

        // Wait a little
        Thread.sleep(500);

        // Remove ingredient
        onView(withId(com.project.giniatovia.feature_fridge.R.id.image_close))
                .perform(click());

        // Check whether "Show recipes" is not clickable
        onView(withId(com.project.giniatovia.feature_fridge.R.id.main_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}
