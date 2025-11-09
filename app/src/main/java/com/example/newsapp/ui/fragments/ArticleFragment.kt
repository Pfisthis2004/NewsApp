package com.example.newsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentArticleBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar


// Fragment hiển thị chi tiết 1 bài báo
class ArticleFragment : Fragment(R.layout.fragment_article) {

    // ViewModel dùng để thao tác dữ liệu (lấy từ NewsActivity)
    lateinit var newsViewModel: NewsViewModel

    // Safe Args: nhận đối tượng Article được truyền từ fragment khác
    val args: ArticleFragmentArgs by navArgs()

    // ViewBinding để thao tác với layout fragment_article.xml
    lateinit var binding: FragmentArticleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Gắn binding với view đã inflate
        binding = FragmentArticleBinding.bind(view)

        // Lấy ViewModel từ Activity cha (NewsActivity)
        newsViewModel = (activity as NewsActivity).newsViewModel

        // Lấy article được truyền vào từ Safe Args
        val article = args.article

        // Hiển thị web page trong WebView
        binding.webView.apply {
            // Đặt WebViewClient để khi click link thì mở ngay trong WebView (không nhảy ra Chrome)
            webViewClient = WebViewClient()
            // Nếu url của article khác null thì load trang
            article.url?.let {
                loadUrl(it)
            }
        }

        // Khi click vào FloatingActionButton (nút tròn)
        binding.fab.setOnClickListener {
            // Gọi ViewModel để thêm bài báo vào mục yêu thích
            newsViewModel.addToFavourites(article)
            // Hiện thông báo Snackbar ở dưới màn hình
            Snackbar.make(view, "Added to favourite", Snackbar.LENGTH_SHORT).show()
        }
    }
}
