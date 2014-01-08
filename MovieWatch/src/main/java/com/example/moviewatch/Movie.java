package com.example.moviewatch;

/**
 * Created by dx165-xl on 2014-01-08.
 */
public class Movie {
    private long id;
    private int MovieId;
    private String MovieTitle;
    private int MovieCritics;
    private int MovieAudience;
    private String Mpaa;
    private String ImageUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public int getMovieId() {
        return MovieId;
    }

    public void setMovieId(int MovieId) {
        this.MovieId = MovieId;
    }
    public String getMovieTitle() {
        return MovieTitle;
    }

    public void setMovieTitle(String MovieTitle) {
        this.MovieTitle = MovieTitle;
    }
    public int getMovieCritics() {
        return MovieCritics;
    }

    public void setMovieCritics(int MovieCritics) {
        this.MovieCritics = MovieCritics;
    }
    public int getMovieAudience() {
        return MovieAudience;
    }

    public void setMovieAudience(int MovieAudience) {
        this.MovieAudience = MovieAudience;
    }
    public String getMpaa() {
        return Mpaa;
    }

    public void setMpaa(String Mpaa) {
        this.Mpaa = Mpaa;
    }
    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String ImageUrl) {
        this.ImageUrl = ImageUrl;
    }

}
