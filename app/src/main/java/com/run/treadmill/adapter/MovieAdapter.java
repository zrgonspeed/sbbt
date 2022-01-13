package com.run.treadmill.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;

import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
public class MovieAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private List<Movie> mMovies;
    private OnItemClickListener mListener;

    public MovieAdapter(List<Movie> movies) {
        mMovies = movies;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        holder.img_movie.setImageResource(movie.getMovieImgId());
        holder.tv_movie_name.setText(movie.getMovieName());

        if (mListener != null) {
            holder.rl_movie.setOnClickListener(v -> {
                mListener.onItemClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void addItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public static class Movie {
        private int movieImgId;
        private int showMovieImgId;
        private String movieName;

        public int getMovieImgId() {
            return movieImgId;
        }

        public void setMovieImgId(int movieImgId) {
            this.movieImgId = movieImgId;
        }

        String getMovieName() {
            return movieName;
        }

        public void setMovieName(String movieName) {
            this.movieName = movieName;
        }

        public int getShowMovieImgId() {
            return showMovieImgId;
        }

        public void setShowMovieImgId(int showMovieImgId) {
            this.showMovieImgId = showMovieImgId;
        }
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout rl_movie;
    ImageView img_movie;
    TextView tv_movie_name;

    MyViewHolder(View itemView) {
        super(itemView);
        rl_movie = (RelativeLayout) itemView.findViewById(R.id.rl_movie);
        img_movie = (ImageView) itemView.findViewById(R.id.img_movie);
        tv_movie_name = (TextView) itemView.findViewById(R.id.tv_movie_name);
    }
}