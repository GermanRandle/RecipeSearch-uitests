package com.erya.recipesearch.presentation.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.erya.recipesearch.data.repository.PageRepositoryImpl
import com.erya.recipesearch.presentation.viewmodels.ViewModelFactory
import com.project.giniatovia.core.db.data.RecipeRoomDatabase
import com.project.giniatovia.core.network.implementation.*
import com.project.giniatovia.feature_recipe.data.repository.RecipesRepositoryImpl
import com.project.giniatovia.feature_fridge.R
import com.project.giniatovia.feature_fridge.data.ProductRepositoryImpl
import com.project.giniatovia.feature_fridge.databinding.FragmentFridgeBinding
import com.project.giniatovia.feature_fridge.presentation.ProductAdapter
import com.project.giniatovia.core.network.models.Product
import com.project.giniatovia.feature_fridge.presentation.viewmodels.ProductViewModel
import com.project.giniatovia.feature_recipe.data.datasource.RecipeDataSource
import com.project.giniatovia.feature_recipe.presentation.models.UiItemError
import okhttp3.logging.HttpLoggingInterceptor

class FridgeFragment : Fragment() {

    private var _binding: FragmentFridgeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        val recipeDataSource = RecipeDataSource(
            RecipeApiImpl(
                RetrofitImpl(
                    ConverterFactoryImpl(), OkHttpClientImpl(
                        InterceptorImpl.Release()
                    )
                )
            ).recipesService(),
        )
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                RecipesRepositoryImpl(
                    recipeDataSource,
                    RecipeRoomDatabase.getDatabase(requireContext()).recipeDao(),
                ),
                ProductRepositoryImpl(
                    requireContext(),
                    RecipeRoomDatabase.getDatabase(requireContext()).recipeDao(),
                    recipeDataSource
                ),
                PageRepositoryImpl()
            )
        ).get(ProductViewModel::class.java)
        viewModel.getAllProducts()
        viewModel.getProductsFromDb()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.toolbar
        toolbar.navigationContentDescription = "Go back"
        toolbar.setNavigationIcon(com.project.giniatovia.feature_recipe.R.drawable.ic_back_green)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val lifecycleOwner = viewLifecycleOwner
        viewModel.allProductLiveData.observe(lifecycleOwner) { uiItemError ->
            when (uiItemError) {
                is UiItemError.Success -> {
                    val autoCompleteAdapter = ArrayAdapter(
                        requireContext(),
                        R.layout.item_search_product,
                        R.id.nameProduct,
                        uiItemError.elements!!
                    )
                    val autoCompleteWidget = binding.autoCompleteTextView
                    autoCompleteWidget.threshold = 1
                    autoCompleteWidget.setAdapter(autoCompleteAdapter)
                    autoCompleteWidget.setTextColor(Color.BLACK)
                    autoCompleteWidget.setOnItemClickListener { _, _, _, _ ->
                        viewModel.add(autoCompleteWidget.editableText.toString())
                        autoCompleteWidget.setText(R.string.empty)

                        val context = context
                        val imm =
                            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(autoCompleteWidget.windowToken, 0)
                    }
                }
                is UiItemError.Error -> {
                    // TODO: Show errors
                    Log.d("TAG", uiItemError.exception.toString())
                }
                is UiItemError.Loading -> {
                    //todo: show progress bar
                }
            }

        }

        viewModel.productLiveData.observe(lifecycleOwner) { uiItemError ->
            when (uiItemError) {
                is UiItemError.Success -> {
                    if (uiItemError.elements?.size == 0) {
                        handleEmptyScreen()
                    } else {
                        handleFilledScreen(uiItemError)
                    }
                    val productAdapter = binding.rv.adapter
                    if (productAdapter == null) {
                        val myAdapter = ProductAdapter(
                            onDeleteProductClick = { viewModel.deleteProduct(it) }
                        )
                        binding.rv.adapter = myAdapter
                        binding.rv.layoutManager = LinearLayoutManager(requireContext())
                        myAdapter.submitList(uiItemError.elements)
                    } else {
                        val myAdapter = productAdapter as ProductAdapter
                        myAdapter.submitList(uiItemError.elements)
                    }
                }
                is UiItemError.Error -> {
                    // TODO: Show errors
                    Log.d("TAG", uiItemError.exception.toString())
                }
                is UiItemError.Loading -> {
                    //todo: show progress bar
                }
            }
        }
    }

    private fun handleFilledScreen(uiItemError: UiItemError.Success<List<Product>>) {
        val lottieImg = binding.lottieProducts
        lottieImg.clearAnimation()
        lottieImg.visibility = View.GONE

        binding.emptyTV.visibility = View.GONE
        binding.mainBtn.visibility = View.VISIBLE
        binding.secondaryBtn.visibility = View.GONE

        binding.mainBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container, RecipeListFragment.newInstance(
                        makeList(uiItemError.elements) as ArrayList<String>
                    )
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun handleEmptyScreen() {
        val lottieImg = binding.lottieProducts
        lottieImg.setAnimation(R.raw.empty_products_lottie)
        lottieImg.repeatCount = LottieDrawable.INFINITE
        lottieImg.visibility = View.VISIBLE
        lottieImg.playAnimation()

        binding.emptyTV.visibility = View.VISIBLE
        binding.secondaryBtn.visibility = View.VISIBLE
        binding.mainBtn.visibility = View.GONE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun makeList(value: List<Product>?): List<String> {
        return value?.map { usedProduct -> usedProduct.name }.orEmpty()
    }
}