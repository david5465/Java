
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ScreenRecorder {
	private static final int SCREEN_WIDTH = 1920; // Ancho de la pantalla
    private static final int SCREEN_HEIGHT = 1080; // Alto de la pantalla
    private static final int FPS = 30; // Cuadros por segundo
    private static final int RECORDING_TIME = 10; // Tiempo de grabación en segundos
    private static final int BITS_PER_SAMPLE = 16; // Bits por muestra de audio
    private static final int CHANNELS = 2; // Número de canales de audio
    private static final float SAMPLE_RATE = 44100; // Tasa de muestreo de audio

    public static void main(String[] args) {
        try {
            // Inicializar la grabación de audio
            AudioFormat audioFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS,
                    (BITS_PER_SAMPLE / 8) * CHANNELS, SAMPLE_RATE, false);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            // Inicializar la grabación de pantalla
            Rectangle screenRect = new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT);
            Robot robot = new Robot();
            long startTime = System.currentTimeMillis();

            // Crear carpeta para guardar la grabación
            File outputFolder = new File("output");
            if (!outputFolder.exists()) {
                outputFolder.mkdir();
            }

            // Realizar la grabación
            while ((System.currentTimeMillis() - startTime) / 1000 < RECORDING_TIME) {
                // Capturar pantalla
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                File outputFile = new File(outputFolder, System.currentTimeMillis() + ".png");
                ImageIO.write(screenCapture, "png", outputFile);

                // Capturar audio
                byte[] audioData = new byte[targetDataLine.getBufferSize() / 5];
                targetDataLine.read(audioData, 0, audioData.length);

                // Guardar audio en un archivo WAV
                AudioInputStream audioStream = new AudioInputStream(
                        new ByteArrayInputStream(audioData), audioFormat, audioData.length / audioFormat.getFrameSize());
                File audioFile = new File(outputFolder, System.currentTimeMillis() + ".wav");
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            }

            // Detener la grabación de audio
            targetDataLine.stop();
            targetDataLine.close();
        } catch (LineUnavailableException | AWTException | IOException e) {
            e.printStackTrace();
        }
    }
}
