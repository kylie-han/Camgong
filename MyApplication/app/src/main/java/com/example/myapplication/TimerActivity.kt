package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.models.Result
import com.example.myapplication.timecamera.CameraSourcePreview
import com.example.myapplication.timecamera.GraphicOverlay
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.FaceDetector.Builder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import kotlinx.android.synthetic.main.activity_timer.*
import java.io.IOException
import java.util.*


class TimerActivity : AppCompatActivity() {
    private val TAG = "FaceTracker"

    private var mCameraSource: CameraSource? = null

    private var mPreview: CameraSourcePreview? = null
    private var mGraphicOverlay: GraphicOverlay? = null

    private val RC_HANDLE_GMS = 9001
    // permission request codes need to be < 256
    private val RC_HANDLE_CAMERA_PERM = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상태바 숨기기
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 화면 켜짐 유지
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_timer)
        var intent = getIntent()
        var time: Long= intent.getLongExtra("time", 0)
        chronometer.base=SystemClock.elapsedRealtime()+time;
        chronometer.start();
        mPreview = findViewById<View>(R.id.preview) as CameraSourcePreview
        mGraphicOverlay = findViewById<View>(R.id.faceOverlay) as GraphicOverlay

        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
        } else {
            requestCameraPermission()
        }

        btnPause.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            chronometer.stop();
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) +1
            val day = calendar.get(Calendar.DATE)
            var date = "$year"
            if(month<10)
            {
                date+="0$month"
            }else
            {
                date+="$month"
            }
            if(day<10)
            {
                date+="0$day"
            }else
            {
                date+="$day"
            }
            val uid = FirebaseAuth.getInstance().uid
            Log.d("타이머", "" + uid)
            time = chronometer.base-SystemClock.elapsedRealtime()
            val ref =FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")
            Log.d("파베 경로", "" + ref)
            ref.setValue(Result(emptyList(),0,0,time))
            Log.d("time1111", "" + time)
            AlertDialog.Builder(this)
                .setMessage("기록되었습니다.")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .show()
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                AlertDialog.Builder(this)
                    .setMessage("기록되었습니다.")
                    .setPositiveButton("OK",
                        DialogInterface.OnClickListener { dialog, which -> finish() })
                    .show()

            }
        }
        return true
    }
    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val thisActivity: Activity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }
        Snackbar.make(
            mGraphicOverlay!!, R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok, listener)
            .show()
    }


    private fun createCameraSource() {
        val context: Context = applicationContext

        val detector: FaceDetector = Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setProminentFaceOnly(true)
            .build()

        detector.setProcessor(

            object : Detector.Processor<Face>{
                override fun release() {
                    mGraphicOverlay?.clear()
                }

                override fun receiveDetections(detections: Detections<Face>) {
                    mGraphicOverlay?.clear()
                    var faces = detections?.detectedItems
                    if(faces.size()>0)
                    {
                        val graphic = FaceGraphic(mGraphicOverlay, faces.valueAt(0))
                        mGraphicOverlay?.add(graphic)
                    }else{
                        //
                    }

                }
            }
//            MultiProcessor.Builder(GraphicFaceTrackerFactory(mGraphicOverlay!!))
//                .build()
        )
        if (!detector.isOperational()) {

            Log.w(TAG, "Face detector dependencies are not yet available.")
        }
        mCameraSource = CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .build()
    }

    /**
     * Restarts the camera.
     */
    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    /**
     * Stops the camera.
     */
    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    private fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg: Dialog =
                GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }
        if (mCameraSource != null) {
            try {
                mPreview!!.start(mCameraSource, mGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            // we have permission, so create the camerasource
            createCameraSource()
            return
        }
        Log.e(
            TAG, "Permission not granted: results len = " + grantResults.size +
                    " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)"
        )
        val listener =
            DialogInterface.OnClickListener { dialog, id -> finish() }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Face Tracker sample")
            .setMessage(R.string.no_camera_permission)
            .setPositiveButton(R.string.ok, listener)
            .show()
    }
}
