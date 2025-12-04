package com.example.pomodoro.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.ItemSoundBinding
import com.example.pomodoro.SettingsActivity
import com.example.pomodoro.adapter.SoundAdapter
import com.example.pomodoro.util.SoundManager
import com.example.pomodoro.SoundItem

class SoundAdapter(
    private val ctx: Context,
    private val items: List<SoundItem>,
    private var selectedRes: Int,
    private val onSelect: (SoundItem) -> Unit
) : RecyclerView.Adapter<SoundAdapter.VH>() {

    inner class VH(val binding: ItemSoundBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvSoundName.text = item.name
        holder.binding.rbSelected.isChecked = (item.res == selectedRes)

        holder.binding.btnPreview.setOnClickListener {
            SoundManager.playSound(ctx, item.res)
        }

        holder.binding.root.setOnClickListener {
            selectedRes = item.res
            notifyDataSetChanged()
            onSelect(item)
        }

        holder.binding.rbSelected.setOnClickListener {
            selectedRes = item.res
            notifyDataSetChanged()
            onSelect(item)
        }
    }
}