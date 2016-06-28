package videoslider.terry.com.videoslider;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String YOUTUBE_KEY = "AIzaSyANi0yWVmzh-3yAF0HREKkZTar3j7_EFkc";

    private static final int CLICK_ACTION_THRESHHOLD = 5;

    private ViewPager mYoutubeViewPager;
    private BannerAdapter mBannerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mYoutubeViewPager = (ViewPager) findViewById(R.id.video_pager);
        mBannerAdapter = new BannerAdapter(getSupportFragmentManager(), new ArrayList<BannerListItem>());
        mYoutubeViewPager.setAdapter(mBannerAdapter);
        mYoutubeViewPager.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP: {
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isClick(startX, endX, startY, endY)) {
                            int index = (mYoutubeViewPager.getCurrentItem()) % mBannerAdapter.getList().size();
                            String videoId = mBannerAdapter.getList().get(index).getVideoId();
                            if (!TextUtils.isEmpty(videoId)) {
                                Intent intent = YouTubeIntents.createPlayVideoIntentWithOptions(
                                        MainActivity.this, videoId, true, false);
                                startActivity(intent);
                            }
                        }

                        break;
                    }
                }
                return false;
            }

            private boolean isClick(float startX, float endX, float startY, float endY) {
                float diffX = Math.abs(startX - endX);
                float diffY = Math.abs(startY - endY);
                if (diffX > CLICK_ACTION_THRESHHOLD || diffY > CLICK_ACTION_THRESHHOLD) {
                    return false;
                }
                return true;
            }
        });
        mBannerAdapter.add(new BannerListItem("7xtB9hcl1R4"));
        mBannerAdapter.add(new BannerListItem("EwfW3NxN2qA"));
    }

    /**
     * ViewPager adapter
     */
    private static class BannerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<BannerListItem> bannerListItem;

        public BannerAdapter(FragmentManager fm, ArrayList<BannerListItem> bannerListItem) {
            super(fm);
            this.bannerListItem = bannerListItem;
        }

        public void add(BannerListItem item) {
            bannerListItem.add(item);
            notifyDataSetChanged();
        }

        public ArrayList<BannerListItem> getList() {
            return bannerListItem;
        }

        @Override
        public Fragment getItem(int position) {
            String videoId;
            if (bannerListItem.size() > 0) {
                videoId = bannerListItem.get(position % bannerListItem.size()).getVideoId();
            } else {
                videoId = bannerListItem.get(position).getVideoId();
            }
            return BannerFragment.create(position, videoId);
        }

        @Override
        public int getCount() {
            if (bannerListItem.size() > 0) {
                return Integer.MAX_VALUE;
            } else {
                return bannerListItem.size();
            }
//            return bannerListItem.size();
        }
    }

    /**
     * Using fragment to show slide imageView
     */
    public static class BannerFragment extends Fragment
            implements YouTubeThumbnailView.OnInitializedListener, YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        private static final String ARG_POSITION = "position";
        private static final String ARG_VIDEO_ID = "video_id";

        private int mPosition;
        private String mYoutubeVideoId;
        private YouTubeThumbnailView mYouTubeThumbnailView;
        private YouTubeThumbnailLoader mThumbnailLoader;
        private ProgressBar mProgressBar;

        public BannerFragment() {
        }

        public static BannerFragment create(int position, String videoId) {
            BannerFragment fragment = new BannerFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_POSITION, position);
            args.putString(ARG_VIDEO_ID, videoId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPosition = getArguments().getInt(ARG_POSITION);
            mYoutubeVideoId = getArguments().getString(ARG_VIDEO_ID);
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_banner, container, false);
            mYouTubeThumbnailView = (YouTubeThumbnailView) view.findViewById(R.id.thumbnail);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
            mYouTubeThumbnailView.initialize(YOUTUBE_KEY, this);
            return view;
        }

        @Override
        public void onDestroyView() {
            if (mThumbnailLoader != null)
                mThumbnailLoader.release();
            super.onDestroyView();
        }

        @Override
        public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView,
                                            YouTubeThumbnailLoader youTubeThumbnailLoader) {
            mThumbnailLoader = youTubeThumbnailLoader;
            mThumbnailLoader.setOnThumbnailLoadedListener(this);
            mThumbnailLoader.setVideo(mYoutubeVideoId);
        }

        @Override
        public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
            Log.d(TAG, "onInitializationFailure= " + youTubeInitializationResult.toString());
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
            Log.d(TAG, "onThumbnailLoaded");
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
            Log.d(TAG, "onThumbnailError= " + errorReason.toString());
        }
    }
}
