package com.erya.recipesearch.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.erya.recipesearch.R
import com.erya.recipesearch.data.repository.PageRepositoryImpl
import com.erya.recipesearch.presentation.viewmodels.ViewModelFactory
import com.erya.recipesearch.RecipesApplication
import com.project.giniatovia.core.network.implementation.*
import com.project.giniatovia.feature_recipe.data.repository.RecipesRepositoryImpl
import com.project.giniatovia.feature_fridge.data.ProductRepositoryImpl
import com.project.giniatovia.feature_recipe.data.datasource.RecipeDataSource
import com.project.giniatovia.feature_recipe.databinding.FragmentListRecipeBinding
import com.project.giniatovia.feature_recipe.presentation.adapters.RecipesAdapter
import com.project.giniatovia.feature_recipe.presentation.models.UiItemError
import com.project.giniatovia.feature_recipe.presentation.viewmodels.RecipeViewModel
import okhttp3.logging.HttpLoggingInterceptor

class RecipeListFragment : Fragment() {

    private var _binding: FragmentListRecipeBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListRecipeBinding.inflate(inflater, container, false)
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

        val args = arguments?.getStringArrayList(SELECTED_PRODUCTS)
        viewModel.clearLiveData()
        if (args != null) {
            viewModel.getRecipeByIngredients(args)
        } else {
            viewModel.getSavedRecipes()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lifecycleOwner = viewLifecycleOwner

        viewModel.recipeLiveData.observe(lifecycleOwner) { uiItemError ->
            when (uiItemError) {
                is UiItemError.Success -> {
                    // Success Response
                    binding.progressBar.visibility = View.GONE
                    val productAdapter = binding.rvRecipe.adapter
                    if (productAdapter == null) {
                        val myAdapter = RecipesAdapter(
                            onRecipeClick = {
                                val fragment = RecipeDialogFragment.newInstance(it.id!!)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        )
                        binding.rvRecipe.adapter = myAdapter
                        binding.rvRecipe.layoutManager = GridLayoutManager(requireContext(),2)
                        myAdapter.submitList(uiItemError.elements)
                    } else {
                        val myAdapter = productAdapter as RecipesAdapter
                        myAdapter.submitList(uiItemError.elements)
                    }
                }
                // TODO: Show errors
                is UiItemError.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Log.d("TAG", uiItemError.exception.toString())
                }
            }
        }
    }

    companion object {
        private const val SELECTED_PRODUCTS = "selected_products"

        fun newInstance(args: ArrayList<String>) = RecipeListFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(SELECTED_PRODUCTS, args)
            }
        }
    }
}