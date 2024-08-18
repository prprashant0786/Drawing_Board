package com.example.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {


    private var drawingView: DrawingView? = null

    private var mImageButtonpaint : ImageButton? = null

    var progrssdia : Dialog? = null

private val opengallauncher : ActivityResultLauncher<Intent> = // For getting image
    registerForActivityResult((ActivityResultContracts.StartActivityForResult())){
        result->

        if(result.resultCode== RESULT_OK && result.data!=null){
            val imagebackgrd :ImageView = findViewById(R.id.iv_background)

            imagebackgrd.setImageURI(result.data?.data)
        }
    }

    private val externalstoragelauncherpermision : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){permissions->
            permissions.entries.forEach{
                val permissionName = it.key
                //Todo 3: if it is granted then we show its granted
                val isGranted = it.value

                if(isGranted){
                    if (permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){

                        val pickIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                        opengallauncher.launch(pickIntent)

                    }
                }
                else{
                    if (permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(
                            this,
                            "Permission Denied for Storage",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        drawingView = findViewById(R.id.drawingview)

        drawingView?.setsizeforbrush(10f)

        val llpaintcolor : LinearLayout = findViewById(R.id.ll_paintcolor)
        mImageButtonpaint = findViewById(R.id.bl)
        mImageButtonpaint?.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_presed)
        )




        var ibgallary : ImageButton = findViewById(R.id.ib_galbut)

        ibgallary.setOnClickListener {
            requeststoragepermison()
        }


        val ibbrush : ImageButton = findViewById(R.id.ib_brushsize)

        ibbrush.setOnClickListener {
            showtheBrushSizeDialoge()
        }

        val inundo : ImageButton = findViewById(R.id.ibundo)


        inundo.setOnClickListener {
            drawingView?.onclickundo()
        }

        inundo.setOnLongClickListener {
            drawingView?.oneraseeverything()
            true
        }

        var ibsave : ImageButton = findViewById(R.id.ibsave)

        ibsave.setOnClickListener {
            if(isReadStorageAllowed()){
                showprogress()
                lifecycleScope.launch{
                    val fldrawingview : FrameLayout = findViewById(R.id.fl_drawing_conata)
                    saveBitmapfile(getBitMapFromView(fldrawingview))
                }
            }
        }

        ibsave.setOnLongClickListener {
            val fldrawingview : FrameLayout = findViewById(R.id.fl_drawing_conata)
            val bitmap = getBitMapFromView(fldrawingview)

            val path = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "IMAGE DESCRIPTION",
                null
            )

            val uri = Uri.parse(path)

            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/*"
            share.putExtra(Intent.EXTRA_TEXT,"MADE BY PANDATJI_786")
            share.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(share, "share image"))

            true
        }

    }


    private fun isReadStorageAllowed(): Boolean {

        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        //If permission is granted returning true and If permission is not granted returning false
        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun requeststoragepermison() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ) showRationaleDialog("Permission Denied For Storage",
        "Permission is required to use image from files")

        else {
           externalstoragelauncherpermision.launch(
               arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                   Manifest.permission.WRITE_EXTERNAL_STORAGE
               )
           )
        }
    }

    fun showtheBrushSizeDialoge(){

        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialogue_brush_size)
        brushDialog.setTitle("Brush Size:")

        brushDialog.window?.setGravity(Gravity.BOTTOM)
        val smallbtn : ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallbtn.setOnClickListener(View.OnClickListener {
            drawingView?.setsizeforbrush(5.toFloat())
            brushDialog.dismiss()
        })

        val midbut : ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        midbut.setOnClickListener(View.OnClickListener {
            drawingView?.setsizeforbrush(10.toFloat())
            brushDialog.dismiss()
        })

        val larbtn : ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        larbtn.setOnClickListener(View.OnClickListener {
            drawingView?.setsizeforbrush(20.toFloat())
            brushDialog.dismiss()
        })

        brushDialog.show()
    }

    fun paintclicked(view: View){
        if(view != mImageButtonpaint){
            val imageButton = view as ImageButton
            val colortag = imageButton.tag.toString()

            drawingView?.setColor(colortag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_presed)
            )

            mImageButtonpaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )

            mImageButtonpaint = view
        }
    }

    private fun showRationaleDialog(
        //use when denied
        title: String,
        message: String,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }



    //FOR STORING FILE
    private fun getBitMapFromView(view: View) : Bitmap {
        val returnedbitmap = Bitmap.createBitmap(view.width,
            view.height,Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnedbitmap)
        val bgDrawable = view.background

        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }
        else canvas.drawColor(Color.WHITE)

        view.draw(canvas)
        return returnedbitmap
    }

    private suspend fun saveBitmapfile(bitmap: Bitmap?):String{
        var result = ""

        withContext(Dispatchers.IO){
            if(bitmap!=null){
                try { // due to outputstream

                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,80,bytes)

                    val f = File(externalCacheDir?.absoluteFile.toString()
                            + File.separator + "Drawingapp_" +
                            System.currentTimeMillis()/1000 + ".png"
                     )
                    val fo = FileOutputStream(f)

                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath

                    runOnUiThread {
                        cancelprogress()
                        if (!result.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully :$result",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    }
                catch (e:Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
        }

        return result
    }


    fun showprogress(){
        progrssdia = Dialog(this)

        progrssdia?.setContentView(R.layout.pro)

        progrssdia?.show()
    }

    fun cancelprogress(){
        if(progrssdia!=null){
            progrssdia?.dismiss()
            progrssdia = null
        }
    }


    private fun shareimg(bitmap:Bitmap){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, bitmap)
        startActivity(Intent.createChooser(intent, "Share"))

    }
}