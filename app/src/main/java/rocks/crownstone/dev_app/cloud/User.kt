package rocks.crownstone.dev_app.cloud

import android.content.Context
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import nl.komponents.kovenant.*
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class User(context: Context, volleyQueue: RequestQueue) {
	private val TAG = this.javaClass.simpleName
	private val volleyQueue = volleyQueue
	private val context = context

	private var userData: UserData? = null

	private fun hashPassword(password: String): Promise<String, Exception> {
		val deferred = deferred<String, Exception>()
		try {
			val digest = MessageDigest.getInstance("SHA-1")
			digest!!.reset()
			val bytes = digest.digest(password.toByteArray(charset("UTF-8")))


			val strBuilder = StringBuilder()
			for (b in bytes) {
				strBuilder.append(String.format("%02x", b))
			}
			val hashedPassword = strBuilder.toString()
			deferred.resolve(hashedPassword)
		} catch (e: NoSuchAlgorithmException) {
			deferred.reject(e)
		} catch (e: UnsupportedEncodingException) {
			deferred.reject(e)
		}
		return deferred.promise
	}



	private fun loginWithHashedPassword(email: String, hashedPassword: String): Promise<String, Exception> {
		val deferred = deferred<String, Exception>()
		val url = "https://my.crownstone.rocks/api/users/login"
		val data = JSONObject()
		data.put("email", email)
		data.put("password", hashedPassword)
		Log.i(TAG,"data: " + data.toString())
		val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, data,
				Response.Listener { response ->
					Log.i(TAG, "Response: %s".format(response.toString()))
					deferred.resolve(response.toString())
				},
				Response.ErrorListener { error ->
					Log.e(TAG, "Error: %s".format(error.toString()))
					deferred.reject(Exception("Login failed: $error"))
				}
		)

		volleyQueue.add(jsonObjectRequest)
		return deferred.promise
	}

	private fun parseLoginResponse(loginResponse: String): Promise<Unit, Exception> {
		return task {
			val json = JSONObject(loginResponse)
			val userId = json.getString("userId")
			val accessToken = json.getString("id")
			val ttl = json.getLong("ttl")
			val creationDate = json.getString("created")
			userData = UserData(userId, accessToken, ttl, creationDate)
		}
	}


	fun login(email: String, password: String): Promise<Unit, Exception> {
		return hashPassword(password)
				.then {
					loginWithHashedPassword(email, it)
				}.unwrap()
				.then {
					parseLoginResponse(it)
				}.unwrap()
	}

	fun getUserData(): Promise<UserData, Exception> {
		val deferred = deferred<UserData, Exception>()
		val data = userData
		if (data == null) {
			deferred.reject(Exception("not logged in"))
			return deferred.promise
		}
		// TODO: check if access token is valid
		deferred.resolve(data)
		return deferred.promise
	}

	fun saveLogin(context: Context) {
		val prefs = getDefaultSharedPreferences(context)
		val prefsEditor = prefs.edit()
		val userData = this.userData ?: return
		prefsEditor.putString("user.id", userData.id)
		prefsEditor.putString("user.accessToken", userData.accessToken)
		prefsEditor.putLong("user.ttl", userData.ttl)
		prefsEditor.putString("user.creationDate", userData.creationDate)
		prefsEditor.apply()
	}

	fun forgetLogin(context: Context) {
		val prefs = getDefaultSharedPreferences(context)
		val prefsEditor = prefs.edit()
		val userData = this.userData ?: return
		prefsEditor.remove("user.id")
		prefsEditor.remove("user.accessToken")
		prefsEditor.remove("user.ttl")
		prefsEditor.remove("user.creationDate")
		prefsEditor.apply()
	}

	fun loadLogin(context: Context): Boolean {
		val prefs = getDefaultSharedPreferences(context)

		val userId = prefs.getString("user.id", null) ?: return false
		val accessToken = prefs.getString("user.accessToken", null) ?: return false
		val creationDate = prefs.getString("user.creationDate", null) ?: return false
		val ttl = prefs.getLong("user.ttl", 0)
		if (ttl == 0L) {
			return false
		}
		userData = UserData(userId, accessToken, ttl, creationDate)
		return true
	}
}