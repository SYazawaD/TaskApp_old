package com.yuzawa.shota.techacademy.jp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PointF.length
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import io.realm.RealmResults
import io.realm.RealmQuery
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.content_input.*


const val EXTRA_TASK = "com.yuzawa.shota.techacademy.jp.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
                reloadListView()
        }
    }
    //TaskAdapterを保持するプロパティを定義する
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        //onCreateメソッドでTaskAdapterを生成する
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, inputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)
        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, view, position, id ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, inputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }
            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()
            true
        }
        category_search_on.setOnClickListener(){
            reloadListView()
        }
                reloadListView()
    }
    //TaskAdapterにデータを設定、ListViewにTaskAdapterを設定、再描画する
    private fun reloadListView() {
        if (category_search.text.toString().isEmpty()) {
            val taskRealmResults = mRealm.where(Task::class.java)
                .findAll().sort("date", Sort.DESCENDING)
            // 上記の結果を、TaskList としてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        } else{
        val taskRealmResults = mRealm.where(Task::class.java)
            .equalTo("category", category_search.text.toString())
            .findAll().sort("date", Sort.DESCENDING)
            // 上記の結果を、TaskList としてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        }
        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }
    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    private fun addTaskForTest() {
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()
    }

    private fun reloadListSearch() {
        val taskRealmResults = mRealm.where(Task::class.java)
            .equalTo("category","category_search.text").findAll().sort("date", Sort.DESCENDING)
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }
}