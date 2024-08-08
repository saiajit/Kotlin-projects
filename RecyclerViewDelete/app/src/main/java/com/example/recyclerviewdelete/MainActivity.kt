package com.example.recyclerviewdelete

import ItemAdap
import android.content.ClipData.Item
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerviewdelete.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

private lateinit var binding: ActivityMainBinding
private lateinit var itemAdap:ItemAdap
private var mList = arrayListOf<String>()
private val deleteIc1 = R.drawable.ic_android_black_24dp
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        itemAdap= ItemAdap()
        mData()
        SwipeDel()
    }
    private fun mData(){
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")
        mList.add("Apptmyz")

        itemAdap.differ.submitList(mList)

        binding.recyclerView2.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter= itemAdap
            setHasFixedSize(true)
        }
    }
    private fun SwipeDel() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
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
                val pos= viewHolder.adapterPosition
                val item = itemAdap.differ.currentList[pos]
                mList.removeAt(pos)
                itemAdap.notifyItemRemoved(pos)
                Snackbar.make(
                    binding.rootView,
                    "Item $item $pos Deleted",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setAction("Undo"){
                        mList.add(item)
                    }
                    show()
                }
            }
//            override fun onChildDraw(
//                c: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                RecyclerViewSwipe.Builder(
//                    c,
//                    recyclerView,
//                    viewHolder,
//                    dX,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//                    .addSwipeLeftLabel("Delete")
//                    .setSwipeLeftLabelColor(resources.getColor(R.color.black))
//                    .addSwipeLeftActionIcon(deleteIc1)
//                    .addSwipeLeftBackgroundColor(resources.getColor(R.color.black))
//                    .addSwipeRightBackgroundColor(resources.getColor(R.color.black))
//                    .addSwipeRightActionIcon(deleteIc1)
//                    .create()
//                    .decorate()
//
//            }

        }).attachToRecyclerView(binding.recyclerView2)


    }
}