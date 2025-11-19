package com.example.newsapp.ui

import android.app.Application
import android.content.Context
import android.icu.text.StringSearch
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsReponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import okhttp3.Connection
import okio.IOException
import retrofit2.Response
import java.util.Locale

class NewsViewModel(app: Application, val newsRepository: NewsRepository): AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsReponse>> = MutableLiveData()
    var headlinesPage=1
    var headlinesResponse: NewsReponse? = null
    val searchNews: MutableLiveData<Resource<NewsReponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsReponse: NewsReponse?= null
    var newSearchQuery: String?= null
    var oldSearchQuery: String?=null
    private val maxResult =10

    init {
        getHeadLines("vn")
    }

    fun getHeadLines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }
    fun nextPage(countryCode: String) {
        headlinesPage++
        getHeadLines(countryCode)
    }

    fun prevPage(countryCode: String) {
        if (headlinesPage > 1) {
            headlinesPage--
            getHeadLines(countryCode)
        }
    }
    private fun handHeadLinesResponse(response: Response<NewsReponse>): Resource<NewsReponse>{
        return if (response.isSuccessful) {
            response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
        } else {
            Resource.Error(response.message())
        }
    }

    private fun handleSearchNewsReponse(reponse: Response<NewsReponse>): Resource<NewsReponse>{
        if (reponse.isSuccessful){
            reponse.body()?.let {
                    resultResponse ->
                if (searchNewsReponse == null || newSearchQuery!= oldSearchQuery){
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsReponse = resultResponse
                }else{
                    searchNewsPage++
                    val oldArticle = searchNewsReponse?.articles
                    val newArticle = resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(reponse.message())
    }
    fun addToFavourites(article: Article)= viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavoriteNews()

    fun deletaArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun InternetConnection(context: Context): Boolean {
        // Lấy ra ConnectivityManager (hệ thống quản lý kết nối mạng của Android)
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Lấy "network đang hoạt động" (active network), nếu null thì nghĩa là không có mạng
        val network = connectivityManager.activeNetwork ?: return false

        // Lấy thông tin chi tiết về khả năng của mạng (NetworkCapabilities)
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Kiểm tra mạng đó hỗ trợ loại transport nào (WiFi / Dữ liệu di động / Ethernet)
        return when {
            // Nếu có WiFi → true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Nếu có 3G/4G/5G (Cellular) → true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // Nếu có Ethernet (cắm dây mạng LAN) → true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true

            // Các trường hợp còn lại (Bluetooth, VPN, ... chưa check) → false
            else -> false
        }
    }
    private suspend fun headlinesInternet(countryCode: String) {
        // 1) Phát tín hiệu "đang tải" để UI hiển thị ProgressBar/State loading
        headlines.postValue(Resource.Loading())

        try {
            // 2) Kiểm tra kết nối mạng (dùng Application context từ AndroidViewModel)
            if (InternetConnection(this.getApplication())) {

                // 3) Gọi API lấy headlines từ repository (theo trang headlinesPage)
                val reponse = newsRepository.getHeadlines(countryCode, lang ="vi", page = headlinesPage,maxResult)

                // 4) Xử lý phản hồi: gộp trang, tăng page nếu thành công, bọc vào Resource.Success / Error
                headlines.postValue(handHeadLinesResponse(reponse))
            } else {
                // 5) Không có mạng → phát ra trạng thái lỗi để UI hiển thị thông báo
                headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            // 6) Bắt lỗi khi gọi mạng: IOException (timeout/mất mạng giữa chừng) hay lỗi khác
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
        // 1) Lưu lại query tìm kiếm hiện tại vào biến toàn cục (newSearchQuery)
        newSearchQuery = searchQuery

        // 2) Bắn ra trạng thái "Loading" để UI biết là đang tìm kiếm
        searchNews.postValue(Resource.Loading())

        try {
            // 3) Kiểm tra có mạng không
            if (InternetConnection(this.getApplication())) {

                // 4) Gọi API searchNews trong repository, truyền vào query và page hiện tại
                val reponse = newsRepository.searchNews(searchQuery, lang ="vi", countryCode = "vn",maxResult)

                // 5) Xử lý response (gộp dữ liệu, tăng page, hoặc báo lỗi)
                searchNews.postValue(handleSearchNewsReponse(reponse))
            } else {
                // 6) Nếu không có mạng thì báo lỗi luôn
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            // 7) Bắt ngoại lệ trong lúc gọi API
            when (t) {
                // Nếu là IOException (timeout, mất kết nối, DNS fail…)
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
                // Các lỗi khác (parse JSON lỗi, null, v.v.)
                else -> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }

}