import ItemAdap.ItemViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.recyclerviewdelete.databinding.ItemLayoutBinding

class ItemAdap : RecyclerView.Adapter<ItemViewHolder>() {
    inner class ItemViewHolder(val binding: ItemLayoutBinding):
        ViewHolder(binding.root)

    private val diffCall= object: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffCall)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        ))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
        }
    }

    override fun getItemCount()= differ.currentList.size
}