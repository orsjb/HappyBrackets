package net.happybrackets.sychronisedmodel;

import java.util.ArrayList;
import java.util.List;

public class Renderer {

    List<Diad2d> ligths = new ArrayList<Diad2d>();
    List<Diad2d> speakers = new ArrayList<Diad2d>();

    public Renderer() {

    }

    public void addLight(Diad2d d) {
        ligths.add(d);
    }

    public void addSpeaker(Diad2d d) {
        speakers.add(d);
    }

    public void RenderLight(Diad2d light) {

    }

    public void RenderSpeaker(Diad2d speaker) {

    }

    public void executeRender() {
        for (Diad2d s : speakers) {
            RenderSpeaker(s);
        }
        for (Diad2d l : ligths) {
            RenderLight(l);
        }
    }

    public interface Device {
        int x = 0;
        int y = 0;
        String name = "";
    }
}
