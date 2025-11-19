package com.example.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentFavouritesBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class FavouritesFragment : Fragment(R.layout.fragment_favourites) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    lateinit var binding: FragmentFavouritesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavouritesBinding.bind(view)

        // Lấy ViewModel từ NewsActivity
        newsViewModel = (activity as NewsActivity).newsViewModel

        // Khởi tạo RecyclerView
        setupFavoriteRecycler()

        // Khi click vào 1 bài báo -> chuyển sang ArticleFragment để đọc chi tiết
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_favouritesFragment_to_articleFragment,
                bundle
            )
        }

        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                newsViewModel.deletaArticle(article)
                Snackbar.make(view,"Xóa khỏi bản tin yêu thích?", Snackbar.LENGTH_LONG).apply {
                    setAction("Hủy"){
                        newsViewModel.addToFavourites(article)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallBack).apply{
            attachToRecyclerView(binding.recyclerFavourites)
        }

        newsViewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer{articles ->
            newsAdapter.differ.submitList(articles)
        })
    }

    private fun setupFavoriteRecycler(){
            newsAdapter = NewsAdapter()
            binding.recyclerFavourites.apply {
                adapter = newsAdapter
                layoutManager = LinearLayoutManager(activity)
            }
    }
}