package com.rafo.pixabay.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rafo.pixabay.api.data.SearchHit
import com.rafo.pixabay.databinding.ImageListItemBinding
import com.rafo.pixabay.util.load

class MainAdapter(
    private var imageList: MutableList<SearchHit>,
    val itemClickListener: ((SearchHit) -> Unit)
) : RecyclerView.Adapter<MainAdapter.MainHolder>() {

    inner class MainHolder(private val binding: ImageListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(searchHit: SearchHit) {
            binding.itemImage.load(searchHit.webformatURL)
            binding.itemImage.setOnClickListener {
                itemClickListener.invoke(searchHit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ImageListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    fun setData(hits: List<SearchHit>) {
        imageList.addAll(hits)
        notifyDataSetChanged()
    }

    fun clear() {
        imageList.clear()
        notifyDataSetChanged()
    }
}