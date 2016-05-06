package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.douncoding.dao.Lesson;
import com.douncoding.dao.Student;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LessonContactFragment extends Fragment {
    public static final String TAG = LessonContactFragment.class.getSimpleName();

    public static final String EXTRA_PARAM1 = "param1";

    public LessonContactFragment() { }

    RecyclerView mContactListView;
    RecyclerView.LayoutManager mLayoutManager;
    ContactsAdapter mAdapter;

    Lesson mLesson;

    AppContext mApp;
    WebService mWebService;
    public static LessonContactFragment newInstance(int lessonId) {
        LessonContactFragment fragment = new LessonContactFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_PARAM1, lessonId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mApp = (AppContext)context.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int lessonId = getArguments().getInt(EXTRA_PARAM1);
            mLesson = mApp.openDBReadable().getLessonDao().load((long)lessonId);
            Log.i(TAG, "강의정보 읽기 완료: 식별자:" + mLesson.getId());
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mWebService = retrofit.create(WebService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson_contacts,
                container, false);

        mAdapter = new ContactsAdapter();
        mLayoutManager = new LinearLayoutManager(getContext());

        mContactListView = (RecyclerView)view.findViewById(R.id.lesson_student_contacts);
        mContactListView.setLayoutManager(mLayoutManager);
        mContactListView.addItemDecoration(new DividerItemDecoration(getContext()));
        mContactListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLesson != null) {
            mWebService.getStudentsOfLesson(mLesson.getId().intValue()).enqueue(new Callback<List<Student>>() {
                @Override
                public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                    if (response.body() != null) {
                        mAdapter.addItem(response.body());
                    } else {
                        Log.w(TAG, "수강생이 없음");
                    }
                }

                @Override
                public void onFailure(Call<List<Student>> call, Throwable t) {

                }
            });
        }
    }

    class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
        ArrayList<Student> mDataset = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_contact, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Student item = mDataset.get(position);

            if (item != null) {
                holder.mNameView.setText(item.getName());
                holder.mEmailView.setText(item.getEmail());
                //holder.mFaceView.setImageBitmap();
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void addItem(List<Student> items) {
            mDataset.addAll(items);
            notifyDataSetChanged();
        }

        public class ViewHolder extends  RecyclerView.ViewHolder
            implements View.OnClickListener {

            ImageView mFaceView;
            TextView mNameView;
            TextView mEmailView;
            ImageView mPhoneView;

            public ViewHolder(View itemView) {
                super(itemView);

                mFaceView = (ImageView)itemView.findViewById(R.id.user_face);
                mNameView = (TextView)itemView.findViewById(R.id.user_name);
                mEmailView = (TextView)itemView.findViewById(R.id.user_email);
                mPhoneView = (ImageView)itemView.findViewById(R.id.user_phone);
                mPhoneView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.user_phone) {
                    Student item = mDataset.get(getPosition());
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+item.getPhone()));
                    startActivity(callIntent);
                }
            }
        }
    }

    public class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{android.R.attr.listDivider};

        private Drawable mDivider;

        /**
         * Default divider will be used
         */
        public DividerItemDecoration(Context context) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            mDivider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        /**
         * Custom divider will be used
         */
        public DividerItemDecoration(Context context, int resId) {
            mDivider = ContextCompat.getDrawable(context, resId);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
