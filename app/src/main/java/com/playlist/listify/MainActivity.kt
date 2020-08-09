package com.playlist.listify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.playlist.listify.Const.CLIENT_ID
import com.playlist.listify.Const.REDIRECT_URI
import com.playlist.listify.Const.REQUEST_CODE
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kaaes.spotify.webapi.android.SpotifyApi
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Request


object Const {
    const val CLIENT_ID =""
    const val REDIRECT_URI =""
    const val REQUEST_CODE = 1234
}

object SpotifySampleContexts {
    const val TRACK_URI = "spotify:track:4IWZsfEkaK49itBwCTFDXQ"
    const val ALBUM_URI = "spotify:album:4m2880jivSbbyEGAKfITCa"
    const val ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2"
    const val PLAYLIST_URI = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF"
    const val PODCAST_URI = "spotify:show:2tgPYIeGErjk6irHRhk9kj"
}

class MainActivity : AppCompatActivity() {

    lateinit var mSpotifyAppRemote: SpotifyAppRemote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        button_first.setOnClickListener {
//            mSpotifyAppRemote.playerApi.play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL")

        }
    }

    fun connected(){
        mSpotifyAppRemote.userApi.subscribeToUserStatus().setEventCallback {
            Log.w("warning", it.longMessage)
        }
        mSpotifyAppRemote.playerApi
            .subscribeToPlayerState()
            .setEventCallback { playerState: PlayerState ->
                val track: Track? = playerState.track
                if (track != null) {
                    assertAppRemoteConnected()
                        .imagesApi
                        .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                        .setResultCallback { bitmap ->
                            current_song_cover.setImageBitmap(bitmap)
//                            mSpotifyAppRemote.imagesApi.getImage(track.imageUri)
                            song_title.text = track.name
                        }
                }
            }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        mSpotifyAppRemote.let {
            if (it.isConnected) {
                return it
            }
        }
        throw SpotifyDisconnectedException()
    }

    override fun onStart() {
        super.onStart()
        authorize()
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    fun funn(){
        SpotifyApi()
    }
    private fun authorize(){

        val builder: AuthorizationRequest.Builder =
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)

        builder.setScopes(arrayOf("streaming"))
        val request: AuthorizationRequest = builder.build()

        AuthorizationClient.openLoginActivity(this, Const.REQUEST_CODE, request)


        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("MainActivity", "Connected! Yay!")

                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                }
            })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show()

        // Check if result comes from the correct activity
//        if (requestCode == LoginActivity.REQUEST_CODE) {
            val response: AuthorizationResponse =
                AuthorizationClient.getResponse(resultCode, intent)
            Log.w("TOKEN:", response.accessToken)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    Toast.makeText(this, response.accessToken, Toast.LENGTH_SHORT).show()
                }
                AuthorizationResponse.Type.ERROR -> {
                }
                else -> {
                }
            }
//        } else Log.e("ERR", "WHYYYYYYYYYYYYYYYYYY")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}