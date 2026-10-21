package com.bcorps.learning;

import java.util.List;

/**
 * Revision notes for one track: short sections in Markdown, plus one-shot
 * YouTube videos for a quick revision before an exam.
 */
public record TutorialNote(Track track, String overview, List<Section> sections, List<Video> videos,
                           List<Link> links) {

    public record Section(String title, String markdown) { }

    /** A full-course ("one shot") video. language = Hindi or English. */
    public record Video(String title, String channel, String language, String duration, String youtubeId) {
        public String url() {
            return "https://www.youtube.com/watch?v=" + youtubeId;
        }
    }

    public record Link(String title, String url) { }
}
