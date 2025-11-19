package com.example.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentHeadlinesBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Resource

class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {

    // ViewModel để quản lý dữ liệu tin tức
    lateinit var newsViewModel: NewsViewModel

    // Adapter cho RecyclerView (hiển thị danh sách tin tức)
    lateinit var newsAdapter: NewsAdapter

    // View hiển thị khi có lỗi
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemHeadlinesError: CardView

    // ViewBinding cho layout fragment_headlines.xml
    lateinit var binding: FragmentHeadlinesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlinesBinding.bind(view) // liên kết binding với layout

        // View hiển thị lỗi trong layout
        itemHeadlinesError = view.findViewById(R.id.itemHeadlinesError)

        // Inflate layout item_error.xml để lấy retryButton và errorText
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        // Lấy ViewModel từ NewsActivity
        newsViewModel = (activity as NewsActivity).newsViewModel

        // Khởi tạo RecyclerView
        setupHeadlinesRecycler()

        // Khi click vào 1 bài báo -> chuyển sang ArticleFragment để đọc chi tiết
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_headlinesFragment_to_articleFragment,
                bundle
            )
        }
        binding.btnNextPage.setOnClickListener {
            newsViewModel.nextPage("vn")
            binding.tvCurrentPage.text = newsViewModel.headlinesPage.toString()
        }

        binding.btnPrevPage.setOnClickListener {
            newsViewModel.prevPage("vn")
            binding.tvCurrentPage.text = newsViewModel.headlinesPage.toString()
        }

        // Quan sát LiveData headlines từ ViewModel
        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsReponse ->
                        // Đưa dữ liệu vào RecyclerView (submitList dùng AsyncListDiffer)
                        newsAdapter.differ.submitList(newsReponse.articles.toList())

                        // Tính tổng số trang (dựa vào totalResults và QUERY_PAGE_SIZE)
                        val totalPages = newsReponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinesPage == totalPages

                        // Nếu tới trang cuối thì bỏ padding dưới RecyclerView
                        if (isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "lỗi: $message",
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

        // Nút retry khi load lỗi
        retryButton.setOnClickListener {
            newsViewModel.getHeadLines("vn")
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
        itemHeadlinesError.visibility = View.INVISIBLE
        isError = false
    }

    // Hiện thông báo lỗi
    private fun showErrorMessage(message: String) {
        itemHeadlinesError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    // Listener phân trang khi cuộn RecyclerView
    val srollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNoError && isNotLoadingAndNotLastPage && isAtLastItem &&
                        isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                newsViewModel.getHeadLines("vn")
                isScrolling = false
            }

            // Hiển thị nút chuyển trang khi cuộn đến cuối
            binding.paginationControls.visibility = if (isAtLastItem) View.VISIBLE else View.GONE
        }

        // Kiểm tra khi người dùng bắt đầu cuộn (touch scroll)
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }
    }



    // Khởi tạo RecyclerView với adapter và scrollListener
    private fun setupHeadlinesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlinesFragment.srollListener)
        }
    }

}
