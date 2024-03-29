package rocks.crownstone.dev_app.ui.control

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import rocks.crownstone.dev_app.R

//private val TAB_TITLES = arrayOf(
//		R.string.tab_text_1,
//		R.string.tab_text_2
//)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, val extras: Bundle?)
	: FragmentPagerAdapter(fm) {

	override fun getItem(position: Int): Fragment {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class below).
		Log.i("SectionsPagerAdapter", "getItem pos=$position")
		val deviceAddress = extras?.getString("deviceAddress")

		when (position) {
			0 -> return ControlFragment.newInstance(position + 1, deviceAddress)
			1 -> return ConfigFragment.newInstance(position + 1, deviceAddress)
			2 -> return DebugFragment.newInstance(position + 1, deviceAddress)
			3 -> return ServiceDataFragment.newInstance(position + 1, deviceAddress)
			4 -> return BehaviourFragment.newInstance(position + 1, deviceAddress)
			5 -> return MicroappFragment.newInstance(position + 1, deviceAddress)
			6 -> return TestFragment.newInstance(position + 1, deviceAddress)
		}
		return ControlFragment.newInstance(position + 1, deviceAddress)
	}

	override fun getPageTitle(position: Int): CharSequence? {
		Log.i("SectionsPagerAdapter", "getPageTitle pos=$position")
//		return context.resources.getString(TAB_TITLES[position])
		when (position) {
			0 -> return "Control"
			1 -> return "Config"
			2 -> return "Debug"
			3 -> return "Service data"
			4 -> return "Behaviour"
			5 -> return "uApp"
			6 -> return "Test"
		}
		throw Exception("Invalid position")
	}

	override fun getCount(): Int {
		// Number of total pages.
		return 7
	}
}