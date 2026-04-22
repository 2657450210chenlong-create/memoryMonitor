package com.chen.memorymonitor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chen.memory.monitor.MemorySample
import com.chen.memorymonitor.databinding.ItemMemorySampleBinding

class SampleAdapter : ListAdapter<MemorySample, SampleAdapter.SampleViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMemorySampleBinding.inflate(inflater, parent, false)
        return SampleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SampleViewHolder(
        private val binding: ItemMemorySampleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sample: MemorySample) {
            binding.tvSampleTime.text = MemoryFormatters.formatTimestamp(sample.timestampMs)
            binding.tvSampleDetail.text = itemView.context.getString(
                R.string.sample_detail_template,
                MemoryFormatters.formatBytes(sample.javaHeapUsedBytes),
                MemoryFormatters.formatBytes(sample.nativeHeapAllocatedBytes),
                MemoryFormatters.formatBytes(sample.nativeHeapFreeBytes)
            )
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MemorySample>() {
            override fun areItemsTheSame(oldItem: MemorySample, newItem: MemorySample): Boolean {
                return oldItem.timestampMs == newItem.timestampMs
            }

            override fun areContentsTheSame(oldItem: MemorySample, newItem: MemorySample): Boolean {
                return oldItem == newItem
            }
        }
    }
}
