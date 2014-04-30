package mobi.dadoudou.diaodiao;

import android.os.Environment;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        try {
            MP3File mp3 = new MP3File(file.getAbsolutePath());
            AbstractID3v2 id3v2 = mp3.getID3v2Tag();
            ID3v1 id3v1 = mp3.getID3v1Tag();
            if (id3v2 != null) {

                System.out.println("id3v2");

                System.out.println(id3v2.getAlbumTitle());//专辑名
                System.out.println(id3v2.getSongTitle());//歌曲名
                System.out.println(id3v2.getLeadArtist());//歌手

                music.setAlbum(id3v2.getAlbumTitle());
                music.setName(id3v2.getSongTitle());
                music.setAuthor(id3v2.getLeadArtist());

            } else {
                System.out.println("id3v1");

                System.out.println(id3v1.getAlbumTitle());
                System.out.println(id3v1.getSongTitle());
                System.out.println(id3v1.getLeadArtist());

                music.setAlbum(id3v1.getAlbumTitle());
                music.setName(id3v1.getSongTitle());
                music.setAuthor(id3v1.getLeadArtist());
            }
            /*
            AbstractLyrics3 lrc3Tag = mp3.getLyrics3Tag();
            if (lrc3Tag != null) {
                String lyrics = lrc3Tag.getSongLyric();
                System.out.println(lyrics);
            }
            */
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        }
        return music;
    }

    public List<Music> getAllMusicList() {
        return musicList;
    }

}
