package com.example.androidsocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private String username;
    private Socket socket;
    private EditText edtMSG;
    private TextView tvMSG;
    private Button btnSend;
    private TextView welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtMSG = (EditText) findViewById(R.id.edtMSG);
        tvMSG = (TextView) findViewById(R.id.tvMSG);
        btnSend = (Button) findViewById(R.id.btnSend);
        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);


        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        Log.d("Hello", "onCreate: "+username);

        {
            try {
                socket = IO.socket("http://192.168.1.91:3000");
                socket.emit("join",username);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        socket.emit("new message");
        socket.on("chat message", onNewMessage);
        Welcome("Chào mừng đến với phòng chat !", "");
        socket.connect();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend();
            }
        });
    }

    private void attemptSend() {
        String message = edtMSG.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        edtMSG.setText("");

        // Tạo một đối tượng JSONObject chứa thông tin người dùng và nội dung tin nhắn
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put("username", username);
            messageObject.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Gửi tin nhắn kèm thông tin người dùng đến server
        socket.emit("chat message", messageObject);
    }
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Object data = args[0];
                    if (data instanceof String) {
                        // Nếu dữ liệu là chuỗi thông báo "Welcome"
                        String welcomeMessage = (String) data;
                        Welcome("", welcomeMessage);
                    } else if (data instanceof JSONObject){
                        JSONObject obj = (JSONObject) args[0];
                        String message;
                        String username;
                        try {
                            username = obj.getString("username");
                            message = obj.getString("message");
                            addMessage(username,message);
                        } catch (JSONException e) {
                            return;
                        }
                    }


                    // add the message to view

                }
            });
        }
    };
    private void Welcome(String username, String message) {
        welcomeMessage.append(username + " " + message + "\n");
    }

    private void addMessage(String username, String message) {
        tvMSG.append(username + ":" + message + "\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("new message", onNewMessage);
    }
}