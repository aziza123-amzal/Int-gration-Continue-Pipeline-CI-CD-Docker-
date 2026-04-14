package SOUNDS;

import java.io.File;
import javax.sound.sampled.*;

public class WavPlayer {
    private Clip clip = null;
    private AudioInputStream audioStream = null;

    public WavPlayer(File f) throws Exception {
        audioStream = AudioSystem.getAudioInputStream(f);
        AudioFormat audioFormat = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(
                Clip.class, audioStream.getFormat(),
                ((int) audioStream.getFrameLength() * audioFormat.getFrameSize()));
        clip = (Clip) AudioSystem.getLine(info);
    }

    public boolean open() {
        if (clip != null && !clip.isOpen()) {
            try {
                clip.open(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void close() {
        if (clip != null && clip.isOpen()) {
            clip.close();
        }
    }

    public void play() {
        if (clip != null && clip.isOpen()) {
            clip.start();
        }
    }

    public void stop() {
        if (clip != null && clip.isOpen()) {
            clip.stop();
        }
    }
}
