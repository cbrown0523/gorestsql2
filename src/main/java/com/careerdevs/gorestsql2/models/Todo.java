package com.careerdevs.gorestsql2.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Todo {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;


        private long userId;
        private String title;
        private String due_on;
        private String status;

        public long getId() {
            return id;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getTitle() {
            return title;
        }

        public String getDue_on() {
            return due_on;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return "Todos{" +
                    "id=" + id +
                    ", user_id=" + userId +
                    ", title='" + title + '\'' +
                    ", due_on='" + due_on + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

