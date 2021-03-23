package com.rafo.pixabay.ui.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rafo.pixabay.BaseFragment
import com.rafo.pixabay.R
import com.rafo.pixabay.databinding.MainFragmentBinding
import com.rafo.pixabay.ui.detail.DetailFragment.Companion.PIXABY_ITEM
import com.rafo.pixabay.ui.dialog.InternetConBottomSheet
import com.rafo.pixabay.ui.dialog.InternetConBottomSheet.Companion.INTERNET_CON
import com.rafo.pixabay.util.*
import com.tobibur.pagination.PageListener
import com.tobibur.pagination.PaginationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainFragment : BaseFragment(), PageListener {

    private val viewModel: MainViewModel by viewModels()

    private var binding: MainFragmentBinding? = null

    private lateinit var mainAdapter: MainAdapter
    private lateinit var connectionLiveData: ConnectionLiveData

    private var word = ""
    private var bottomSheetOpened = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        connectionLiveData = ConnectionLiveData(requireContext())
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        this.binding = binding
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.run {
            mainAdapter = MainAdapter(mutableListOf()) {
                navController().navigate(
                    R.id.action_mainFragment_to_detailFragment,
                    bundleOf(PIXABY_ITEM to it)
                )
            }
            createMenu()
            val bottomSheet = InternetConBottomSheet.newInstance()
            connectionLiveData.observe(viewLifecycleOwner, { isConnected ->
                if (isConnected) {
                    if (bottomSheetOpened) {
                        bottomSheet.dismiss()
                        mainAdapter.clear()
                        bottomSheetOpened = false
                    }
                    loading.visible()
                    viewModel.search(query = word)
                } else {
                    bottomSheetOpened = true
                    bottomSheet.show(childFragmentManager, INTERNET_CON)
                }
            })
            val gridLayoutManager = GridLayoutManager(requireContext(), 3)
            imagesList.adapter = mainAdapter
            imagesList.layoutManager = gridLayoutManager
            PaginationUtils.initPagination(imagesList, gridLayoutManager, this@MainFragment)
            viewModel.imagesResponseLiveData
                .onEach { response ->
                    response?.let {
                        mainAdapter.setData(response.hits)
                        loading.gone()
                        swipe.isRefreshing = false
                    }
                }.launchWhenStarted(lifecycleScope)
            swipe.setOnRefreshListener {
                mainAdapter.clear()
                viewModel.search(query = word)
            }
        }
    }

    private fun MainFragmentBinding.createMenu() {
        toolbar.title = getString(R.string.app_name)
        toolbar.inflateMenu(R.menu.main_menu)
        val mSearchMenuItem = toolbar.menu.findItem(R.id.search)
        val searchView: SearchView = mSearchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        word = it
                        mainAdapter.clear()
                        viewModel.search(it)
                        loading.visible()
                        mSearchMenuItem.collapseActionView()
                        requireContext().hideKeyboard(searchView)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onPagination(page: Int) {
        viewModel.search(word, page)
    }
}