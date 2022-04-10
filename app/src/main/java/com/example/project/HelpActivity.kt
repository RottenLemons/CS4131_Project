package com.example.project

import android.os.Bundle
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    lateinit var groupList: ArrayList<String>
    lateinit var childList: ArrayList<String>
    lateinit var mobileCollection: Map<String, List<String>>
    lateinit var expandableListView: ExpandableListView
    lateinit var expandableListAdapter: ExpandableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        createGroupList()
        createCollection()
        expandableListView = findViewById(R.id.helpList)
        expandableListAdapter = ExpandableListAdapter(this, groupList, mobileCollection)
        expandableListView.setAdapter(expandableListAdapter)
        expandableListView.setOnGroupExpandListener(object :
            ExpandableListView.OnGroupExpandListener {
            var lastExpandedPosition = -1
            override fun onGroupExpand(i: Int) {
                if (lastExpandedPosition != -1 && i != lastExpandedPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition)
                }
                lastExpandedPosition = i
            }
        })
        expandableListView.setOnChildClickListener(ExpandableListView.OnChildClickListener { expandableListView, view, i, i1, l ->
            val selected = expandableListAdapter!!.getChild(i, i1).toString()
            true
        })
    }

    private fun loadChild(mobileModels: Array<String>) {
        childList = ArrayList()
        for (model in mobileModels) {
            childList.add(model)
        }
    }

    private fun createGroupList() {
        groupList = ArrayList()
        groupList.add("Taking Photos")
        groupList.add("Generating Puzzles")
        groupList.add("Why is the AI bad?")
        groupList.add("History")
        groupList.add("OCR")
        groupList.add("How to use Editor?")
    }

    private fun createCollection() {
        val samsungModels = arrayOf(
            "Please take a photo such that the whole board can be seen. It does not matter if the pieces/ board are 3D or 2D"
        )
        val googleModels = arrayOf(
            "Puzzles are generated from a database. The database should be updated weekly, so there will be new puzzles weekly"
        )
        val redmiModels = arrayOf("Well, the AI is good in tracking the chessboard. However, I have not trained it that much for the pieces, so there are some problems with it. However, an editor is provided for editing it")
        val vivoModels = arrayOf("All the positions are scanned in history, and also shared in Community Posts. Clicking on the board will cause you to go to the analyser. Long pressing it causes the FEN to be copied.")
        val motorolaModels = arrayOf("Optical Character Recognition was used to scan score sheet. Google ML Kit was used, and it can scan other stuff to do. Also, it is not on me if it is bad :)")
        val lgModels = arrayOf("The editor allows you to edit the FEN. To put in the pieces, drag them to the board. To delete pieces, drag the trash icon to the piece wanted to be deleted")

        mobileCollection = HashMap()
        for (group in groupList!!) {
            if (group == "Taking Photos") {
                loadChild(samsungModels)
            } else if (group == "Generating Puzzles") loadChild(googleModels) else if (group == "Why is the AI bad?") loadChild(
                redmiModels
            ) else if (group == "History") loadChild(vivoModels)
             else if (group == "How to use Editor?") loadChild(lgModels)
             else loadChild(motorolaModels)
            (mobileCollection as HashMap<String, List<String>>).put(group, childList!!)
        }
    }


}