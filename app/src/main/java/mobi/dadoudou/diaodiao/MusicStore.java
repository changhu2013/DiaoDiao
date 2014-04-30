package mobi.dadoudou.diaodiao;

import android.os.Environment;
import android.util.Log;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.dadoudou.diaodiao.mp3.Mp3ReadId3v1;
import mobi.dadoudou.diaodiao.mp3.Mp3ReadId3v2;

public class MusicStore {

    private static MusicStore musicStore;

    public static MusicStore getMusicStore() {
        if (musicStore == null) {
            musicStore = new MusicStore();
        }
        return musicStore;
    }

    private MusicStore() {
        initMusicList();
    }

    private List<Music> musicList;

    private void initMusicList() {
        musicList = new ArrayList<Music>();
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File dir = Environment.getExternalStorageDirectory();
            //找到资源目录
            File mymusic = new File(dir, "mymusic");
            if (!mymusic.exists()) {
                mymusic.mkdirs();
            }
            File file = new File(mymusic, "musicList.txt");
            //如果存在音乐列表文件则从文件中读取音乐列表
            if (file.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String path = null;
                    while ((path = reader.readLine()) != null) {
                        System.out.println(path);
                        File temp = new File(path);
                        if (temp.exists()) {
                            musicList.add(createMusic(temp));
                        }
                    }
                    reader.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            } else {
                //不存在音乐列表文件则查找音乐并写入文件
                findMusic(musicList, dir);
                try {
                    file.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    for (Music music : musicList) {
                        String path = music.getFile().getAbsolutePath();
                        writer.write(path);
                        writer.newLine();
                    }
                    writer.flush();
                    writer.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
        }
    }

    private void findMusic(List<Music> list, File file) {
        if (file.isDirectory()) {
            for (File temp : file.listFiles()) {
                findMusic(list, temp);
            }
        } else {
            String name = file.getName();
            if (name.endsWith(".mp3") && file.length() > 1024 * 1024) {
                list.add(createMusic(file));
            }
        }
    }

    private Music createMusic(File file) {

        Music music = new Music();
        music.setFile(file);
        music.setDirectory(file.getParentFile());
        music.setAlbum("未知");
        music.setName("未知");
        music.setAuthor("未知");
        try {
            Mp3ReadId3v2 id3v2 = new Mp3ReadId3v2(new FileInputStream(file));
            id3v2.readId3v2(1024 * 100);
            music.setAlbum(id3v2.getSpecial());
            music.setAuthor(id3v2.getAuthor());
            music.setName(id3v2.getName());
        } catch (Exception e) {
            try {
                Mp3ReadId3v1 id3v1 = new Mp3ReadId3v1(new FileInputStream(file));
                id3v1.readId3v1();
                music.setAlbum(id3v1.getSpecial());
                music.setAuthor(id3v1.getAuthor());
                music.setName(id3v1.getName());
            } catch (FileNotFoundException e1) {
                music.setAlbum("未知");
                music.setName("未知");
                music.setAuthor("未知");
            }
        }
        return music;
    }

    public List<Music> getAllMusicList() {
        return musicList;
    }

}
