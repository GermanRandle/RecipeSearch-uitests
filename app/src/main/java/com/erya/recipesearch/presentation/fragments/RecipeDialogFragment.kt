package com.erya.recipesearch.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.erya.recipesearch.data.repository.PageRepositoryImpl
import com.erya.recipesearch.presentation.viewmodels.ViewModelFactory
import com.erya.recipesearch.RecipesApplication
import com.project.giniatovia.core.network.implementation.*
import com.project.giniatovia.feature_fridge.data.ProductRepositoryImpl
import com.project.giniatovia.feature_recipe.data.datasource.RecipeDataSource
import com.project.giniatovia.feature_recipe.data.repository.RecipesRepositoryImpl
import com.project.giniatovia.feature_recipe.databinding.RecipeDialogBinding
import com.project.giniatovia.feature_recipe.presentation.models.UiItemError
import com.project.giniatovia.feature_recipe.presentation.viewmodels.RecipeViewModel
import okhttp3.logging.HttpLoggingInterceptor

class RecipeDialogFragment : Fragment() {

    private var _binding: RecipeDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeDataSource = RecipeDataSource(
            RecipeApiImpl(
                RetrofitImpl(
                    ConverterFactoryImpl(), OkHttpClientImpl(
                        InterceptorImpl(
                            HttpLoggingInterceptor.Level.BODY
                        )
                    )
                )
            ).recipesService(),
        )
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                RecipesRepositoryImpl(
                    recipeDataSource,
                    (requireActivity().application as RecipesApplication).databaseRecipe.recipeDao()
                ),
                ProductRepositoryImpl(
                    requireContext(),
                    (requireActivity().application as RecipesApplication).databaseProducts.productDao(),
                    recipeDataSource
                ),
                PageRepositoryImpl()
            )
        ).get(RecipeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecipeDialogBinding.inflate(inflater, container, false)
        val args = arguments?.getInt(ID_RECIPE)
        if (args != null) {
            viewModel.getRecipeInfoById(args)
            viewModel.searchDb(args)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.recipeInfoLiveData.observe(viewLifecycleOwner) { uiItemError ->
            if (uiItemError is UiItemError.Success) {
                Glide.with(binding.recipeDialogImg.context)
                    .load(uiItemError.elements?.image)
                    .circleCrop()
                    .into(binding.recipeDialogImg)

                binding.textRecipe.text = uiItemError.elements?.summary
            }
        }

        val resBookmark = com.project.giniatovia.feature_recipe.R.drawable.ic_bookmark
        val resBookmarkFilled = com.project.giniatovia.feature_recipe.R.drawable.ic_bookmark_filled
        val bookmarkImg = binding.bookmark

        viewModel.isSavedRecipe.observe(viewLifecycleOwner) { isSaved ->
            if (isSaved) {
                bookmarkImg.setImageResource(resBookmarkFilled)
                bookmarkImg.setOnClickListener{
                    viewModel.recipeInfoLiveData.value?.let { recipe ->
                        if (recipe is UiItemError.Success) {
                            viewModel.deleteRecipeDb(recipe.elements!!)
                        }
                    }
                    bookmarkImg.setImageResource(resBookmark)
                }
            } else {
                bookmarkImg.setImageResource(resBookmark)
                bookmarkImg.setOnClickListener{
                    viewModel.recipeInfoLiveData.value?.let { recipe ->
                        if (recipe is UiItemError.Success) {
                            viewModel.insertRecipeDb(recipe.elements!!)
                        }
                    }
                    bookmarkImg.setImageResource(resBookmarkFilled)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ID_RECIPE = "id_recipe"

        fun newInstance(arg1: Int) = RecipeDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ID_RECIPE, arg1)
            }
        }
    }
}