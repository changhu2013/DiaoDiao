package mobi.dadoudou.diaodiao;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    private static String TAG = "MYMUSIC";

    private int ACTION_PLAY = 1;
    private int ACTION_PAUSE = 2;
    private int ACTION_BACKWARD = 3;
    private int ACTION_FORWARD = 4;
    private int UPDATE_SEEKBAR = 5;
    private int PLAY_COMPLATE = 6;

    private MediaPlayer player;

    private List<Music> musicList;

    private int current = 0;
    private boolean pause = false;
    private boolean loop = false;

    private TextView nameTextView;
    private ImageView imageView;

    private SeekBar seekBar;
    private TextView currentTextView;
    private TextView totalTextView;
    private ImageButton startBtn;
    private ImageButton backwardBtn;
    private ImageButton forwardBtn;
    private ImageButton repeatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SysApplication.getInstance().addActivity(this);

        musicList = MusicStore.getMusicStore().getAllMusicList();

        nameTextView = (TextView) findViewById(R.id.nameTextView);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.denglijun);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        currentTextView = (TextView) findViewById(R.id.currentTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);

        startBtn = (ImageButton) findViewById(R.id.startButton);
        backwardBtn = (ImageButton) findViewById(R.id.backwardButton);
        forwardBtn = (ImageButton) findViewById(R.id.forwardButton);
        repeatBtn = (ImageButton) findViewById(R.id.repeatButton);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null && player.isPlaying()) {
                    player.seekTo(progress);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Message m = new Message();
                if (player != null && player.isPlaying()) {
                    m.arg1 = ACTION_PAUSE;
                } else {
                    if (player == null) {
                        player = new MediaPlayer();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                Message m = new Message();
                                m.arg1 = PLAY_COMPLATE;
                                handler.sendMessage(m);
                            }
                        });
                    }
                    m.arg1 = ACTION_PLAY;
                }
                handler.sendMessage(m);
            }
        });

        backwardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Message m = new Message();
                m.arg1 = ACTION_BACKWARD;
                handler.sendMessage(m);
            }
        });

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Message m = new Message();
                m.arg1 = ACTION_FORWARD;
                handler.sendMessage(m);
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null && player.isPlaying()) {
                    player.setLooping(true);
                }
                loop = true;
            }
        });
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == ACTION_PLAY) {
                if(musicList.size() == 0){
                    Toast.makeText(MainActivity.this, "没有音乐文件", Toast.LENGTH_LONG).show();
                    return;
                }
                startBtn.setImageResource(R.drawable.media_pause);
                if (pause && player != null) {
                    player.start();
                    pause = false;
                    Log.d(TAG, "暂停后开始播放");
                } else {
                    playByPosition(current);
                }
            } else if (msg.arg1 == ACTION_PAUSE) {
                startBtn.setImageResource(R.drawable.media_start);
                player.pause();
                pause = true;
            } else if (msg.arg1 == ACTION_FORWARD) {
                startBtn.setImageResource(R.drawable.media_pause);
                if (player.isPlaying()) {
                    player.stop();
                }
                player.reset();
                if (current > 0) {
                    current--;
                } else {
                    current = musicList.size() - 1;
                }
                playByPosition(current);
            } else if (msg.arg1 == ACTION_BACKWARD) {
                startBtn.setImageResource(R.drawable.media_pause);
                if (player.isPlaying()) {
                    player.stop();
                }
                player.reset();
                if (current < musicList.size() - 1) {
                    current++;
                } else {
                    current = 0;
                }
                playByPosition(current);
            } else if (msg.arg1 == UPDATE_SEEKBAR) {

                //更新进度条
                seekBar.setProgress(msg.arg2);
                currentTextView.setText(transTime(msg.arg2));
            } else if (msg.arg1 == PLAY_COMPLATE) {
                startBtn.setImageResource(R.drawable.media_start);
                pause = false;
                seekBar.setProgress(0);
            }
        }
    };

    private void playByPosition(int position) {
        try {

            Music music = musicList.get(position);

            String name = music.getName();
            nameTextView.setText(name);
            Uri uri = Uri.fromFile(music.getFile());

            player.reset();
            player.setDataSource(MainActivity.this, uri);
            player.prepare();
            player.start();
            if (loop) {
                player.setLooping(true);
            }
        } catch (IOException e) {
            player.release();
        }
        //设置进度条
        final int total = player.getDuration();
        totalTextView.setText(transTime(total));
        seekBar.setProgress(0);
        seekBar.setMax(total);

        new Thread(new Runnable() {
            public void run() {
                int current = 0;
                while (player != null && player.isPlaying() && current < total) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    Message m = new Message();
                    m.arg1 = UPDATE_SEEKBAR;
                    current = player.getCurrentPosition();
                    m.arg2 = current;
                    handler.sendMessage(m);
                }
            }
        }).start();
    }

    private String transTime(int time) {
        time = time / 1000;
        int min = time / 60;
        int sec = time % 60;
        return (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setTitle(R.string.tishi);
        b.setMessage(R.string.tuichutishi);
        b.setNegativeButton(R.string.quxiao,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Toast.makeText(MainActivity.this,
                                R.string.quxiao, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        b.setPositiveButton(R.string.queding,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        SysApplication.getInstance().exit();
                    }
                });
        Dialog dialog = b.create();
        dialog.show();
    }
}
