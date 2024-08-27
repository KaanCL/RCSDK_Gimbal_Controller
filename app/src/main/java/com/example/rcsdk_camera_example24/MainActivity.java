package com.example.rcsdk_camera_example24;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.skydroid.fpvplayer.FPVWidget;
import com.skydroid.rcsdk.KeyManager;
import com.skydroid.rcsdk.PayloadManager;
import com.skydroid.rcsdk.PipelineManager;
import com.skydroid.rcsdk.RCSDKManager;
import com.skydroid.rcsdk.SDKManagerCallBack;
import com.skydroid.rcsdk.comm.CommListener;
import com.skydroid.rcsdk.common.Uart;
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith;
import com.skydroid.rcsdk.common.callback.KeyListener;
import com.skydroid.rcsdk.common.error.SkyException;
import com.skydroid.rcsdk.common.payload.AKey;
import com.skydroid.rcsdk.common.payload.C12;
import com.skydroid.rcsdk.common.payload.PayloadType;
import com.skydroid.rcsdk.common.pipeline.Pipeline;
import com.skydroid.rcsdk.key.AirLinkKey;
import com.skydroid.rcsdk.key.RemoteControllerKey;

import android.media.RemoteController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.media.VideoView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    private PayloadManager payloadManager;
    private C12 c12Camera = null;

    LibVLC libVLC;
    VideoView videoView;
    Pipeline pipeline;

    private int yaw = 1000;
    private int pitch = 1000;
    private int preyaw = 1000;
    private int prePitch = 1000;

    public Handler handler = new Handler();


    private TextView chanels , yawText , pitchText ,key= null ;

    private final KeyListener<int[]> keyH16ChannelsListener = new KeyListener<int[]>() {
        @Override
        public void onValueChange(int[] ints, int[] t1) {

                    infoliveData.postValue(Arrays.toString(t1));
                    infoliveyaw.postValue(t1[12]);
                    infolivepitch.postValue(t1[13]);


        }
    };

    private final MutableLiveData<String> infoliveData = new MutableLiveData<String>();
    private final MutableLiveData<Integer> infoliveyaw = new MutableLiveData<Integer>();
    private final MutableLiveData<Integer> infolivepitch = new MutableLiveData<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chanels= findViewById(R.id.chanels);
        yawText = findViewById(R.id.yaw);
        pitchText = findViewById(R.id.pitch);
        videoView = findViewById(R.id.videoView);

       /* infoliveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                chanels.setText(s);
            }
        });*/
        infoliveyaw.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer i) {
                yawText.setText(String.valueOf(i));
                controlCamera(i,true);
            }
        });
        infolivepitch.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer i) {
                pitchText.setText(String.valueOf(i));
                controlCamera(i,false);
            }
        });



        RCSDKManager.INSTANCE.initSDK(this, new SDKManagerCallBack() {
            @Override
            public void onRcConnected() {

             pipeline =  PipelineManager.INSTANCE.createPipeline(Uart.UART0);
             pipeline.setOnCommListener(getCommListener(0,""));

             PipelineManager.INSTANCE.connectPipeline(pipeline);


            }

            @Override
            public void onRcConnectFail(@Nullable SkyException e) {
                Toast.makeText(getApplicationContext(),"RC Bağlanamadı",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRcDisconnect() {
                Toast.makeText(getApplicationContext(),"RC Bağlantısı Koptu",Toast.LENGTH_LONG).show();
            }
        });

        RCSDKManager.INSTANCE.connectToRC();
        connectRTSP();

        c12Camera = (C12)PayloadManager.INSTANCE.getUDPPayload(PayloadType.C12,5000,"192.168.144.108",5000);

        if(c12Camera != null){
            c12Camera.setCommListener(getCommListener(1,"C12"));
            PayloadManager.INSTANCE.connectPayload(c12Camera);


        }

        getChannels();


    }

    private CommListener getCommListener(int type, String tag) {
        return new CommListener() {
            @Override
            public void onConnectSuccess() {
                System.out.println(tag + " Bağlantı Başarılı");
            }

            @Override
            public void onConnectFail(SkyException e) {
                System.out.println(tag + " Bağlantı başarısız oldu" + e);
            }

            @Override
            public void onDisconnect() {
                System.out.println(tag + " Bağlantıyı kesildi");
            }

            @Override
            public void onReadData(byte[] bytes) {
                if (type == 0) {
                    System.out.println(tag + " veri uzunluğu " + bytes.length + " ,,, veri " + new String(bytes));
                }
            }
        };
    }

    private void connectRTSP(){
        libVLC = new LibVLC(this);
        Media media = new Media(libVLC, Uri.parse("rtsp://192.168.144.108:554/stream=1"));
        media.addOption("--aout=opensles");
        media.addOption("--audio-time-stretch");
        media.addOption("-vvv");
        MediaPlayer mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.setMedia(media);
        mediaPlayer.getVLCVout().setVideoSurface(videoView.getHolder().getSurface(), videoView.getHolder());
        mediaPlayer.getVLCVout().setWindowSize(videoView.getWidth(), videoView.getHeight());
        mediaPlayer.getVLCVout().attachViews();
        mediaPlayer.play();

    }

    private void getChannels(){
        KeyManager.INSTANCE.cancelListen(keyH16ChannelsListener);
        KeyManager.INSTANCE.listen(RemoteControllerKey.INSTANCE.getKeyH16Channels(), keyH16ChannelsListener);
    }

    private void controlCamera(float v , boolean yawPitch){

                if (c12Camera!=null){

                    float value = joyStickMap(v,yawPitch);

                    if(yawPitch) {
                        if (value != 0) {
                            c12Camera.controlYaw(value);
                            System.out.println("YAW: " + value);}
                    }
                    else{
                          if(value!=0) {
                              c12Camera.controlPitch(value);
                              System.out.println("PITCH: " + value);}
                    }
                }

        };


    private static float joyStickMap(float inputValue , boolean yawPitch ){

        float inputMin = 1000 , inputMax = 2000 ,  outputMin = 0 , outputMax = 0;


        if(yawPitch){
            outputMin = -150f;
            outputMax = 150f;
        }
        else{
            outputMin = -90f;
            outputMax = 90f;
        }

        return (inputValue - inputMin) * (outputMax - outputMin) / (inputMax - inputMin) + outputMin;

    }

}