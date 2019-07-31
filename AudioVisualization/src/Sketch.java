import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PJOGL;

import processing.opengl.PShader;
import processing.video.Movie;

import java.io.File;

public class Sketch extends PApplet {

    private PShader shade, shade2, shade3;
    private Audio audio;
    private Movie film;
    private PImage img;
    public float zoom;
    public int piMult = 12;
    private PGraphics layerOne, layerTwo, layerThree;

    public void settings() {
            size(1440, 800, P3D);
            PJOGL.profile = 4;
        }

        public void setup() {
            noLoop();
            audio = new Audio();
            audio.initAudio();
            selectInput("Please Select a film to use as a texture", "acquireFilmData");
//        initAudio();
            shade = loadShader("frag.glsl");
            shade2 = loadShader("frag2.glsl");
            shade3 = loadShader("frag3.glsl");
            img = loadImage("image.png");
            img.resize(width, height);
            zoom = 0.05f;
            textureWrap(1);
            layerOne = createGraphics(width, height, P3D);
            layerOne.textureWrap(1);
            layerTwo = createGraphics(width, height, P3D);
            layerTwo.textureWrap(1);
            layerThree = createGraphics(width, height, P3D);
            layerThree.textureWrap(1);

    }
    static private int attempts = 0;

    public void acquireFilmData(File f) {
        if (f == null) {
            println("Wrong file type");
            attempts++;
            if (attempts > 1) {
//                this.exit();
                System.exit(0);
            }
            selectInput("Please Select a film to process", "aquireFilmData");
        } else {
            String filmName = f.getAbsoluteFile().toString();
            String[] test2 = split(filmName, ".");
            String[] compare = { "mp4", "mov" };
            if (test2[1].compareTo(compare[0]) > 0 && test2[1].compareTo(compare[1]) > 0) {
                selectInput("File must be of type mp4 or mov", "aquireFilmData");
            } else {
                film = new Movie(this, filmName);
                film.loop();
                film.volume(0);
//                film.speed(0.5f);
                loop();
            }
        }
    }

    public void movieEvent( Movie m ) {
//        img = m.get();
            m.read();
        }
        public float scale = 1.0f;

        public void draw() {
            if (keyPressed) {
                if (key == 'l') {
                    zoom += 0.01;
                } else if (key == 'k') {
                    zoom -= 0.01;
                }
            }
            layerOne.shader(shade);
            if (film != null) {
                layerOne.beginDraw();
                shade.set("previousFrame", layerOne);
                layerOne.image(film, 0, 0, width, height);
                layerOne.endDraw();
        }
//        audio.smoothingFactor = map(mouseY, 0, height, 0.0001f , 0.95f);
        int freqIndex = (int)map(mouseX, 0, width, 0, audio.inputL.length - 212);
        float startingFreq = (22050.0f / (float)audio.inputL.length) * freqIndex;
        shade.set("offset", map(audio.inputL[2], 0, height / 10, 0, 1));
        shade.set("time", millis() / 1000);
        shade.set("zoomAmount", zoom);
        shade.set("mouseOffset", norm(mouseX, 0, width), norm(mouseY, 0, height));
        shade.set("trans", abs( audio.inputR[20] ) );
        shade.set("piMult", piMult);
        layerTwo.shader(shade2);
        shade2.set("previousFrame", img);
        shade2.set("offset", map(audio.inputL[4], 0, height / 10, 0, 1));
        shade2.set("time", millis() / 1000);
        shade2.set("zoomAmount", zoom);
        shade2.set("mouseOffset", norm(mouseX, 0, width), norm(mouseY, 0, height));
        shade2.set("trans", abs( audio.inputR[12] ) );
        shade2.set("piMult", piMult);
        layerTwo.beginDraw();
        layerTwo.image(layerOne, 0, 0);
        layerTwo.endDraw();
        surface.setTitle(
                "Frame Rate : " + (int)frameRate +
                " freqIndex : " + startingFreq +
                " zoom " + zoom +
                " piMult" + piMult
        );
        layerThree.shader(shade3);
        shade3.set("previousFrame", layerOne);
        shade3.set("offset", map(audio.inputL[8], 0, height / 10, 0, 1));
        shade3.set("time", millis() / 1000);
        shade3.set("zoomAmount", zoom);
        shade3.set("mouseOffset", norm(mouseX, 0, width), norm(mouseY, 0, height));
        shade3.set("trans", abs( audio.inputR[32] ) );
        shade3.set("piMult", piMult);
        layerThree.beginDraw();
        layerThree.image(layerTwo, 0, 0);
        layerThree.endDraw();
        image(layerThree, 0, 0);
        }

    public void keyPressed() {

         if (key == 'm') {
            piMult++;
        } else if (key == 'n') {
            piMult--;
        }

    }

    public void mousePressed() {
            float timeInFilm = map(mouseX, 0, width, 0, film.duration());
            film.jump(timeInFilm);
    }

    public static void main (final String[] args) {

        PApplet.main( new String[] { Sketch.class.getName() });

    }

}
