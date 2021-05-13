package com.ew.snapapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.ew.snapapp.model.Note;
import com.ew.snapapp.repo.Repo;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    Button btnLoadImage, btnSaveImage;

    private ImageView imageResult;
    private EditText imageName;
    private Bitmap currentBitmap;
    private Note currentNote;
    private ProgressDialog mProgressDialog;

    final int RQS_IMAGE1 = 1;

    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;

    int prvX, prvY;

    Paint paintDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadImage = findViewById(R.id.myLoadBtn);
        btnSaveImage = findViewById(R.id.mySaveBtn);
        imageResult = findViewById(R.id.myImageView);
        imageName = findViewById(R.id.imageName);
        String noteID = getIntent().getStringExtra("noteid");
        currentNote = Repo.r().getNoteWith(noteID);
//        imageName.setText(currentNote.getText());

        mProgressDialog = new ProgressDialog(MainActivity.this);

        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.WHITE);
        paintDraw.setStrokeWidth(10);

        //setupAddButton();

        // only navigates right now
        Button chatBtn = findViewById(R.id.myChatBtn);
        chatBtn.setOnClickListener(v -> goToChat());

        // this is for opening photo library and importing image
        btnLoadImage.setOnClickListener(arg0 -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RQS_IMAGE1);
        });

        //touch listener for drawing - this method is very important because it makes it possible to detect
        // the coordinates of the user touches on the screen to make it possible to draw on the imported

        imageResult.setOnTouchListener((v, event) -> {

            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    prvX = x;
                    prvY = y;
                    drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                    prvX = x;
                    prvY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                    break;
            }
            return true;
        });
    }
    //Project position on ImageView to position on Bitmap draw on it

    private void drawOnProjectedBitMap(ImageView iv, Bitmap bm,
                                       float x0, float y0, float x, float y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return;
        }else{

            float ratioWidth = (float)bm.getWidth()/(float)iv.getWidth();
            float ratioHeight = (float)bm.getHeight()/(float)iv.getHeight();

            canvasMaster.drawLine(
                    x0 * ratioWidth,
                    y0 * ratioHeight,
                    x * ratioWidth,
                    y * ratioHeight,
                    paintDraw);
            imageResult.invalidate();
        }
    }

    // this method is for turning the image into a canvas, which can implement the users drawing ontop of the image
    // and save it into 1 image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap tempBitmap;

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case RQS_IMAGE1:
                    source = data.getData();

                    try {
                        //tempBitmap is Immutable bitmap,
                        //cannot be passed to Canvas constructor
                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(source));

                        Bitmap.Config config;
                        if(tempBitmap.getConfig() != null){
                            config = tempBitmap.getConfig();
                        }else{
                            config = Bitmap.Config.ARGB_8888;
                        }

                        //bitmapMaster is Mutable bitmap
                        bitmapMaster = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);

                        canvasMaster = new Canvas(bitmapMaster);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

                        imageResult.setImageBitmap(bitmapMaster);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }
    public void save(View view){ // Will also update an existing note.
        imageResult.buildDrawingCache(true);
        currentBitmap = Bitmap.createBitmap(imageResult.getDrawingCache(true));
        currentNote.setText(imageName.getText().toString());
        Repo.r().updateNoteAndImage(currentNote, currentBitmap);
        System.out.println("you pressed save");
        System.out.println("The bitmap size: " + currentBitmap.getByteCount());
    }
    private void setupAddButton() {
        btnSaveImage = findViewById(R.id.mySaveBtn);
        btnSaveImage.setOnClickListener(e ->{
//            items.add("New note " + items.size());
//            myAdapter.notifyDataSetChanged(); // tell the listView to reload data
            System.out.println("Add Btn pressed");
            Repo.r().addNote("new note");
        });
    }

    /*@Override
    public void receive(byte[] bytes) {
        // figure out, how to get the byte array to an image, and from there to the imageView
    }*/

    public void goToChat(){
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }
}