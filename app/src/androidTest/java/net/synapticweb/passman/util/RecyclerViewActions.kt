package net.synapticweb.passman.util

import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher

//https://stackoverflow.com/a/30338665/6192350
object RecyclerViewActions {
    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController?, view: View) {
                val v: View = view.findViewById(id)
                v.performClick()
            }
        }
    }

    fun typeInChildViewWithId(id : Int, text : String) : ViewAction {
        return object : ViewAction {
            override fun getDescription(): String {
                return "Type text on a child view with specified id."
            }

            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun perform(uiController: UiController?, view: View?) {
                view?.findViewById<EditText>(id)?.setText(text)
            }

        }
    }
}
