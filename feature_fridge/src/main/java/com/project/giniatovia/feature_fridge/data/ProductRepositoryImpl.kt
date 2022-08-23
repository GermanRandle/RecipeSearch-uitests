package com.project.giniatovia.feature_fridge.data

import android.content.Context
import androidx.annotation.WorkerThread
import com.project.giniatovia.core.db.data.ProductDao
import com.project.giniatovia.core.db.models.ProductEntity
import com.project.giniatovia.core.network.data.CSVParser
import com.project.giniatovia.feature_fridge.domain.ProductRepository
import com.project.giniatovia.feature_recipe.data.datasource.RecipeDataSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class ProductRepositoryImpl(
    private val context: Context,
    private val productDao: ProductDao,
    private val dataSource: RecipeDataSource
    ) : ProductRepository {
    override fun getAllProducts(): Single<List<String>> = Single
        .fromCallable { CSVParser(context, "ingredients.csv").parse() }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insertProduct(product: ProductEntity) = productDao.insertProduct(product)

    override suspend fun getSavedProducts(): List<ProductEntity> = productDao.getSavedProducts()

    @WorkerThread
    override suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    override suspend fun getProductImage(product: String) = dataSource.getProductImage(product).image
}