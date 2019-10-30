package com.yandex.mapkitdemo

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.search.Search
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.SuggestItem
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

import java.util.ArrayList

/**
 * This example shows how to request a suggest for search requests.
 */
class SuggestActivity : Activity(), SearchManager.SuggestListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"
    private val RESULT_NUMBER_LIMIT = 5

    private var searchManager: SearchManager? = null
    private var suggestResultView: ListView? = null
    private var resultAdapter: ArrayAdapter<*>? = null
    private var suggestResult: MutableList<String>? = null

    private val CENTER = Point(55.75, 37.62)
    private val BOX_SIZE = 0.2
    private val BOUNDING_BOX = BoundingBox(
            Point(CENTER.latitude - BOX_SIZE, CENTER.longitude - BOX_SIZE),
            Point(CENTER.latitude + BOX_SIZE, CENTER.longitude + BOX_SIZE))
    private val SEARCH_OPTIONS = SearchOptions().setSearchTypes(
            SearchType.GEO.value or
                    SearchType.BIZ.value or
                    SearchType.TRANSIT.value)

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)
        setContentView(R.layout.suggest)
        super.onCreate(savedInstanceState)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        val queryEdit = findViewById<View>(R.id.suggest_query) as EditText
        suggestResultView = findViewById<View>(R.id.suggest_result) as ListView
        suggestResult = ArrayList()
        resultAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                suggestResult!!)
        suggestResultView!!.adapter = resultAdapter

        queryEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                requestSuggest(editable.toString())
            }
        })
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onSuggestResponse(suggest: List<SuggestItem>) {
        suggestResult!!.clear()
        for (i in 0 until Math.min(RESULT_NUMBER_LIMIT, suggest.size)) {
            suggest[i].displayText?.let { suggestResult!!.add(it) }
        }
        resultAdapter!!.notifyDataSetChanged()
        suggestResultView!!.visibility = View.VISIBLE
    }

    override fun onSuggestError(error: Error) {
        var errorMessage = getString(R.string.unknown_error_message)
        if (error is RemoteError) {
            errorMessage = getString(R.string.remote_error_message)
        } else if (error is NetworkError) {
            errorMessage = getString(R.string.network_error_message)
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun requestSuggest(query: String) {
        suggestResultView!!.visibility = View.INVISIBLE
        searchManager!!.suggest(query, BOUNDING_BOX, SEARCH_OPTIONS, this)
    }
}
