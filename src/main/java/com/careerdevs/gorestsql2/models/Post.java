package com.careerdevs.gorestsql2.models;

import javax.persistence.*;

@Entity
public class Post {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private long id;

    private long userId;

    private String title;


    @Column(length = 600)
    private String body;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", user_id=" + userId +
                ", body='" + body + '\'' +
                '}';
    }
}