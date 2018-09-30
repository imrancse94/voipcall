package com.sinch.apptoappcall;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class CallActivity extends AppCompatActivity {

    private static final String APP_KEY = "07f6d0e0-d338-42e2-8625-7bf74d8ce3ee";
    private static final String APP_SECRET = "o2r7nBDgz0KmHXofi8m8Pg==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private ImageButton button;
    private ImageButton cancel;
    private String callerId;
    private String recipientId;
    public MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call);
        mp = MediaPlayer.create(this,R.raw.ringtone);
        Intent intent = getIntent();
        callerId = intent.getStringExtra("callerId");
        recipientId = intent.getStringExtra("recipientId");

        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(callerId)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        button = (ImageButton) findViewById(R.id.receive);
        cancel = (ImageButton) findViewById(R.id.cancel);
        callState = (TextView) findViewById(R.id.callstate);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call == null) {
                    call = sinchClient.getCallClient().callUser(recipientId);
                    call.addCallListener(new SinchCallListener());
                    callState.setText("Hang Up");
                } else {
                    call.hangup();
                }
            }
        });
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            SinchError a = endedCall.getDetails().getError();
            callState.setText("Call");
            callState.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            callState.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            callState.setText("ringing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(final CallClient callClient, final Call incomingCall) {
            mp.start();
            call = incomingCall;
            callState.setText("Ringing");

          //  Toast.makeText(CallActivity.this, "incoming call", Toast.LENGTH_SHORT).show();
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mp.stop();
                    if(call != null) {

                        call.answer();
                        call.addCallListener(new SinchCallListener());
                        callState.setText("Hang Up");
                    }



                    Toast.makeText(CallActivity.this,call.getState().name(), Toast.LENGTH_SHORT).show();

                }
            });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call.hangup();
            }
        });

        }
    }
}

