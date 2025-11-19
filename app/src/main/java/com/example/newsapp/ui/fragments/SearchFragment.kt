package com.example.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.newsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    // ViewModel để quản lý dữ liệu tin tức
    lateinit var newsViewModel: NewsViewModel

    // Adapter cho RecyclerView (hiển thị danh sách tin tức)
    lateinit var newsAdapter: NewsAdapter

    // View hiển thị khi có lỗi
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        itemSearchError = view.findViewById(R.id.itemSearchError)

        // Inflate layout item_error.xml để lấy retryButton và errorText
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        // Lấy ViewModel từ NewsActivity
        newsViewModel = (activity as NewsActivity).newsViewModel

        // Khởi tạo RecyclerView
        setupSearchRecycler()

        // Khi click vào 1 bài báo -> chuyển sang ArticleFragment để đọc chi tiết
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchFragment_to_articleFragment,
                bundle
            )
        }

        var job: Job? = null
        binding.searchEdit.addTextChangedListener(){
            editable->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let{
                    if (editable.toString().isNotEmpty()){
                        newsViewModel.searchNews(editable.toString())
                    }
                }
            }
        }
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsReponse ->
                        // Đưa dữ liệu vào RecyclerView (submitList dùng AsyncListDiffer)
                        newsAdapter.differ.submitList(newsReponse.articles.toList())

                        // Tính tổng số trang (dựa vào totalResults và QUERY_PAGE_SIZE)
                        val totalPages = newsReponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.searchNewsPage == totalPages

                        // Nếu tới trang cuối thì bỏ padding dưới RecyclerView
                        if (isLastPage) {
                            binding.recyclerSearch.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "Lỗi: $message",
                            Toast.LENGTH_LONG
                        ).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })

        retryButton.setOnClickListener {
            if (binding.searchEdit.text.toString().isNotEmpty()){
                newsViewModel.searchNews(binding.searchEdit.text.toString())
            }else{
                hideErrorMessage()
            }
        }
    }


    // Biến trạng thái để kiểm soát phân trang
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    // Ẩn ProgressBar
    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    // Hiện ProgressBar
    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    // Ẩn thông báo lỗi (⚠️ ở đây cũng đang set VISIBLE → nên đổi thành GONE mới hợp lý)
    private fun hideErrorMessage() {
        itemSearchError.visibility = View.INVISIBLE
        isError = false
    }

    // Hiện thông báo lỗi
    private fun showErrorMessage(message: String) {
        itemSearchError.visibility = View.VISIBLE
        isError = true
    }

    val srollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val fistVisibilityItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            // Các điều kiện để phân trang
            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = fistVisibilityItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = fistVisibilityItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNoError && isNotLoadingAndNotLastPage && isAtLastItem &&
                        isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                newsViewModel.searchNews(binding.searchEdit.text.toString())
                isScrolling = false
            }
        }


        // Kiểm tra khi người dùng bắt đầu cuộn (touch scroll)
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }
    }
    private fun setupSearchRecycler(){
        newsAdapter = NewsAdapter()
        binding.recyclerSearch.apply {
            adapter = newsAdapter
            layoutManager= LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.srollListener)
        }
    }
}

