package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("InflateParams")
class MainActivity : AppCompatActivity() {

    private lateinit var manager: SubtitleSetManager
    private lateinit var ids: ArrayList<Int>
    private lateinit var recencyOrder: ArrayList<Int>

    private var checkboxes = ArrayList<ImageView>()
    private var noStarViews = ArrayList<View>()
    private var viewsToDelete = ArrayList<View>()
    private var readingsToDelete = ArrayList<Int>()
    private var checkboxesToDelete = ArrayList<ImageView>()

    private val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = SubtitleSetManager(this)
        initListeners()
        displaySavedReadings()
        if(ids.isEmpty()) openAddSubtitleSet()
    }

    // add button, edit button, and starred only switch listeners
    private fun initListeners() {
        floatingAddButton.setOnClickListener { openAddSubtitleSet() }
        editButton.setOnClickListener { editButtonClicked() }
        starredOnlySwitch.setOnCheckedChangeListener { _, isChecked ->
            beginTransition(homePageParent)
            for(view in noStarViews) view.visibility =
                if(isChecked) View.GONE else View.VISIBLE
        }
    }

    // opens the page to add a new reading when add button pressed
    private fun openAddSubtitleSet() {
        val addSubtitleSetPage = Intent(this, AddSubtitleSetActivity::class.java)
        startActivityForResult(addSubtitleSetPage, requestCode)
    }

    // shows saved reading checkboxes to mark to delete
    private fun editButtonClicked() {
        beginTransition(homePageParent)
        when(editButton.text) {
            resources.getString(R.string.editString) -> {
                resetDeleteLists()
                for(checkbox in checkboxes) checkbox.visibility = View.VISIBLE
                editButton.text = resources.getString(R.string.cancelString)
            }
            resources.getString(R.string.cancelString) -> {
                for(checkbox in checkboxes) checkbox.visibility = View.GONE
                editButton.text = resources.getString(R.string.editString)
            }
            resources.getString(R.string.deleteString) -> {
                for(checkbox in checkboxesToDelete) checkboxes.remove(checkbox)
                for(view in viewsToDelete) {
                    savedReadingsContainer.removeView(view)
                    if(noStarViews.contains(view)) noStarViews.remove(view)
                }
                for(id in readingsToDelete) {
                    manager.deleteSubtitleSet(id)
                    ids = manager.getIds()
                }
                resetDeleteLists()

                for(checkbox in checkboxes) checkbox.visibility = View.GONE
                editButton.text = resources.getString(R.string.editString)
            }
        }
    }

    // clears lists of checked saved readings
    private fun resetDeleteLists() {
        checkboxesToDelete = ArrayList()
        viewsToDelete = ArrayList()
        readingsToDelete = ArrayList()
    }

    // loops through each saved reading and displays on screen
    private fun displaySavedReadings() {
        ids = manager.getIds()
        recencyOrder = ArrayList()
        checkboxes = ArrayList()
        noStarViews = ArrayList()
        val today = manager.getDateString()
        for(id in ids) {
            val subtitleSet = manager.getSubtitleSet(id.toString())
            val savedReadingView =
                layoutInflater.inflate(R.layout.widget_saved_reading, null)
            val daysBetweenDates = getNDaysSinceOpened(today, subtitleSet.dateString)
            fillTextViews(subtitleSet, savedReadingView, daysBetweenDates)

            val checkbox = savedReadingView.findViewById<ImageView>(R.id.checkbox)
            val star = savedReadingView.findViewById<ImageView>(R.id.star)
            if(!subtitleSet.isStarred) noStarViews.add(savedReadingView)
            else star.setImageResource(R.drawable.ic_star_checked)
            checkboxes.add(checkbox)

            var isChecked = false
            checkbox.setOnClickListener { isChecked =
                checkboxClicked(isChecked, savedReadingView, checkbox, id) }
            star.setOnClickListener { starClicked(savedReadingView, subtitleSet, star) }
            savedReadingView.setOnClickListener { openSavedReading(id) }

            savedReadingsContainer.addView(savedReadingView,
                updateRecencyOrder(daysBetweenDates))
        }
    }

    // returns number of days since last updated
    private fun getNDaysSinceOpened(today: String, dateString: String): Int {
        if(today == dateString) return 0
        val todayContents = today.split(":")
        val todayMonth = todayContents[0].toInt()
        val todayDay = todayContents[1].toInt()
        val todayYear = todayContents[2].toInt()
        val dateContents = dateString.split(":")
        val dateMonth = dateContents[0].toInt()
        val dateDay = dateContents[1].toInt()
        val dateYear = dateContents[2].toInt()
        return daysBetweenDates(dateMonth, dateDay, dateYear, todayMonth, todayDay, todayYear)
    }

    // sets the text for each saved reading
    private fun fillTextViews(subtitleSet: SubtitleSetManager.SubtitleSet,
                              savedReadingView: View, daysBetweenDates: Int) {
        val titleTextView =
            savedReadingView.findViewById<TextView>(R.id.titleTextView)
        val detailsTextView =
            savedReadingView.findViewById<TextView>(R.id.detailsTextView)
        val subtitlesTextView =
            savedReadingView.findViewById<TextView>(R.id.subtitlesTextView)

        if(subtitleSet.enteredTitle.isEmpty()) titleTextView.visibility = View.GONE
        else titleTextView.text = subtitleSet.enteredTitle
        detailsTextView.text = resources.getString(R.string.savedReadingDetailsString,
            getTime(subtitleSet.subtitles.size, subtitleSet.enteredText, subtitleSet.speed),
            getPercentRead(subtitleSet.savedSubtitleIndex, subtitleSet.subtitles.size),
            "%", getLastOpened(daysBetweenDates))
        subtitlesTextView.text = getSubtitleText(subtitleSet.enteredText,
            subtitleSet.subtitles, subtitleSet.savedSubtitleIndex)
    }

    // gets the time length of the saved reading
    private fun getTime(size: Int, enteredText: String, speed: String): String {
        val mspw = getMSPW(speed)
        val nWords = countWords(enteredText)
        val totalMS = mspw * nWords + size * subtitleStartDelay
        return getTimeString(totalMS.toInt())
    }

    // gets the amount of percent read of the saved reading
    private fun getPercentRead(index: Int, size: Int): String {
        val progress = if(index == -1) 0 else index
        return (progress.toDouble() / size.toDouble() * 100).toInt().toString()
    }

    // takes the number of days since last opened & returns readable string
    private fun getLastOpened(daysBetweenDates: Int): String {
        return when (daysBetweenDates) {
            0 -> resources.getString(R.string.todayString)
            1 -> resources.getString(R.string.yesterdayString)
            else -> "$daysBetweenDates ${resources.getString(R.string.daysAgoString)}"
        }
    }

    // gets the current subtitle of the saved reading
    private fun getSubtitleText(enteredText: String, subtitles: ArrayList<String>,
                                index: Int): String {
        val text = if(index == -1 || index == subtitles.size)
            enteredText else subtitles[index]
        return if(text.length > maxCharSubtitleToShow)
            "${text.substring(0, maxCharSubtitleToShow).replace('\n', ' ')}..."
        else text.replace('\n', ' ')
    }

    // if saved reading checkbox clicked then it is added to delete
    private fun checkboxClicked(isChecked: Boolean, savedReadingView: View,
                                checkbox: ImageView, id: Int): Boolean {
        return if(isChecked) {
            viewsToDelete.remove(savedReadingView)
            readingsToDelete.remove(id)
            checkboxesToDelete.remove(checkbox)
            if(viewsToDelete.size == 0)
                editButton.text = resources.getString(R.string.cancelString)
            checkbox.setImageResource(R.drawable.ic_box_unchecked)
            false
        } else {
            if(viewsToDelete.size == 0)
                editButton.text = resources.getString(R.string.deleteString)
            viewsToDelete.add(savedReadingView)
            readingsToDelete.add(id)
            checkboxesToDelete.add(checkbox)
            checkbox.setImageResource(R.drawable.ic_box_checked)
            true
        }
    }

    // adds or removes saved reading to starred when clicked
    private fun starClicked(savedReadingView: View,
                            subtitleSet: SubtitleSetManager.SubtitleSet, star: ImageView) {
        if(subtitleSet.isStarred) {
            star.setImageResource(R.drawable.ic_star_unchecked)
            subtitleSet.isStarred = false
            manager.updateSubtitleSet(subtitleSet)
            beginTransition(homePageParent)
            if(starredOnlySwitch.isChecked) savedReadingView.visibility = View.GONE
            if(!noStarViews.contains(savedReadingView))
                noStarViews.add(savedReadingView)
        } else {
            star.setImageResource(R.drawable.ic_star_checked)
            subtitleSet.isStarred = true
            manager.updateSubtitleSet(subtitleSet)
            if(noStarViews.contains(savedReadingView))
                noStarViews.remove(savedReadingView)
        }
    }

    // opens the selected saved reading
    private fun openSavedReading(id: Int) {
        val subtitleReaderPage = Intent(this, SubtitleReaderActivity::class.java)
        subtitleReaderPage.putExtra(idRef, id)
        startActivityForResult(subtitleReaderPage, requestCode)
    }

    // tracks recency order of saved reading
    private fun updateRecencyOrder(daysBetweenDates: Int): Int {
        var indexToAdd = 0
        var addedToList = false
        if(recencyOrder.isNotEmpty()) {
            for(nDaysRecency in recencyOrder)
                if (daysBetweenDates < nDaysRecency) {
                    recencyOrder.add(indexToAdd, daysBetweenDates)
                    addedToList = true
                    break
                } else indexToAdd++
            if(!addedToList) recencyOrder.add(daysBetweenDates)
       } else recencyOrder.add(daysBetweenDates)
        return indexToAdd
    }

    // checkboxes are hidden when returning to this activity
    private fun resetPage() {
        for(checkbox in checkboxesToDelete)
            checkbox.setImageResource(R.drawable.ic_box_unchecked)
        for(checkbox in checkboxes) checkbox.visibility = View.GONE
        resetDeleteLists()
        editButton.text = resources.getString(R.string.editString)
    }

    // saved readings are refreshed if the database has been updated
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(this.requestCode == requestCode) {
            resetPage()
            val dbUpdated = data?.getBooleanExtra(dbUpdatedRef, false)!!
            if(dbUpdated) {
                savedReadingsContainer.removeAllViews()
                displaySavedReadings()
            }
            starredOnlySwitch.isChecked = false
            if(ids.isEmpty()) finish()
        }
    }
}
