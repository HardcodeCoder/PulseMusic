package com.hardcodecoder.pulsemusic.interfaces;

import android.view.View;

import com.hardcodecoder.pulsemusic.model.MusicModel;

public interface ItemClickListener {

    interface Cards {

        /**
         * called upon recycler view item click
         *
         * @param pos passes the adapter position of the clicked item
         */
        void onItemClick(int pos);

        /**
         * called when user clicks on edit button
         *
         * @param pos passes the adapter position
         */
        void onEdit(int pos);
    }

    interface Simple {

        /**
         * called upon recycler view item click
         *
         * @param pos passes the adapter position of the clicked item
         */
        void onItemClick(int pos);

        /**
         * Called when the options button of a recycler view is clicked
         *
         * @param view pases the view for popup menu to work
         * @param pos  passes the position
         */
        void onOptionsClick(View view, int pos);
    }

    interface Selector {

        /**
         * @param md passes the selected item
         */
        void onSelected(MusicModel md);

        /**
         * @param md passes the item previously selected
         */
        void onUnselected(MusicModel md);
    }

    interface SingleEvent {
        /**
         * @param pos Passes the Adapter position of that item clicked
         */
        void onClickItem(int pos);
    }

}
