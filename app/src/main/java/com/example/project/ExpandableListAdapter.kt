package com.example.project

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView


class ExpandableListAdapter(
    private val context: Context, private val groupList: List<String>,
    private val mobileCollection: Map<String, List<String>>
) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return mobileCollection.size
    }

    override fun getChildrenCount(i: Int): Int {
        return mobileCollection[groupList[i]]!!.size
    }

    override fun getGroup(i: Int): Any {
        return groupList[i]
    }

    override fun getChild(i: Int, i1: Int): Any {
        return mobileCollection[groupList[i]]!![i1]
    }

    override fun getGroupId(i: Int): Long {
        return i.toLong()
    }

    override fun getChildId(i: Int, i1: Int): Long {
        return i1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(i: Int, b: Boolean, view: View?, viewGroup: ViewGroup): View? {
        var view : View? = view
        val mobileName = getGroup(i).toString()
        Log.i("HELLO", mobileName)
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.group_item, null)
        }
        val item = view?.findViewById<TextView>(R.id.mobile)
        item?.text = mobileName
        return view
    }

    override fun getChildView(i: Int, i1: Int, b: Boolean, view: View?, viewGroup: ViewGroup): View? {
        var view = view
        val model = getChild(i, i1).toString()
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.child_item, null)
        }
        val item = view?.findViewById<TextView>(R.id.model)
        item?.text = model

        return view
    }

    override fun isChildSelectable(i: Int, i1: Int): Boolean {
        return false
    }
}